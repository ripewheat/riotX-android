/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.riotredesign.features.home.room.detail

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.airbnb.mvrx.fragmentViewModel
import im.vector.matrix.android.api.session.room.timeline.TimelineEvent
import im.vector.riotredesign.R
import im.vector.riotredesign.core.epoxy.LayoutManagerStateRestorer
import im.vector.riotredesign.core.platform.ToolbarConfigurable
import im.vector.riotredesign.core.platform.VectorBaseFragment
import im.vector.riotredesign.features.home.AvatarRenderer
import im.vector.riotredesign.features.home.HomeModule
import im.vector.riotredesign.features.home.HomePermalinkHandler
import im.vector.riotredesign.features.home.room.detail.timeline.TimelineEventController
import im.vector.riotredesign.features.home.room.detail.timeline.helper.EndlessRecyclerViewScrollListener
import im.vector.riotredesign.features.media.MediaContentRenderer
import im.vector.riotredesign.features.media.MediaViewerActivity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_room_detail.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.ext.android.bindScope
import org.koin.android.scope.ext.android.getOrCreateScope
import org.koin.core.parameter.parametersOf


@Parcelize
data class RoomDetailArgs(
        val roomId: String,
        val eventId: String? = null
) : Parcelable


class RoomDetailFragment : VectorBaseFragment(), TimelineEventController.Callback {

    companion object {

        fun newInstance(args: RoomDetailArgs): RoomDetailFragment {
            return RoomDetailFragment().apply {
                setArguments(args)
            }
        }
    }

    private val roomDetailViewModel: RoomDetailViewModel by fragmentViewModel()
    private val timelineEventController: TimelineEventController by inject { parametersOf(this) }
    private val homePermalinkHandler: HomePermalinkHandler by inject()

    private lateinit var scrollOnNewMessageCallback: ScrollOnNewMessageCallback

    override fun getLayoutResId() = R.layout.fragment_room_detail

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindScope(getOrCreateScope(HomeModule.ROOM_DETAIL_SCOPE))
        setupRecyclerView()
        setupToolbar()
        setupSendButton()
        roomDetailViewModel.subscribe { renderState(it) }
    }

    override fun onResume() {
        super.onResume()
        roomDetailViewModel.process(RoomDetailActions.IsDisplayed)
    }

    // PRIVATE METHODS *****************************************************************************

    private fun setupToolbar() {
        val parentActivity = vectorBaseActivity
        if (parentActivity is ToolbarConfigurable) {
            parentActivity.configure(toolbar)
        }
    }

    private fun setupRecyclerView() {
        val epoxyVisibilityTracker = EpoxyVisibilityTracker()
        epoxyVisibilityTracker.attach(recyclerView)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        val stateRestorer = LayoutManagerStateRestorer(layoutManager).register()
        scrollOnNewMessageCallback = ScrollOnNewMessageCallback(layoutManager)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = null
        recyclerView.setHasFixedSize(true)
        timelineEventController.addModelBuildListener {
            it.dispatchTo(stateRestorer)
            it.dispatchTo(scrollOnNewMessageCallback)
        }

        recyclerView.addOnScrollListener(
                EndlessRecyclerViewScrollListener(layoutManager, RoomDetailViewModel.PAGINATION_COUNT) { direction ->
                    roomDetailViewModel.process(RoomDetailActions.LoadMore(direction))
                })
        recyclerView.setController(timelineEventController)
        timelineEventController.callback = this
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val textMessage = composerEditText.text.toString()
            if (textMessage.isNotBlank()) {
                roomDetailViewModel.process(RoomDetailActions.SendMessage(textMessage))
                composerEditText.text = null
            }
        }
    }

    private fun renderState(state: RoomDetailViewState) {
        renderRoomSummary(state)
        timelineEventController.setTimeline(state.timeline)
    }

    private fun renderRoomSummary(state: RoomDetailViewState) {
        state.asyncRoomSummary()?.let {
            toolbarTitleView.text = it.displayName
            AvatarRenderer.render(it, toolbarAvatarImageView)
            if (it.topic.isNotEmpty()) {
                toolbarSubtitleView.visibility = View.VISIBLE
                toolbarSubtitleView.text = it.topic
            } else {
                toolbarSubtitleView.visibility = View.GONE
            }
        }
    }

// TimelineEventController.Callback ************************************************************

    override fun onUrlClicked(url: String) {
        homePermalinkHandler.launch(url)
    }

    override fun onEventVisible(event: TimelineEvent) {
        roomDetailViewModel.process(RoomDetailActions.EventDisplayed(event))
    }

    override fun onMediaClicked(mediaData: MediaContentRenderer.Data, view: View) {
        val intent = MediaViewerActivity.newIntent(vectorBaseActivity, mediaData)
        startActivity(intent)
    }

}
