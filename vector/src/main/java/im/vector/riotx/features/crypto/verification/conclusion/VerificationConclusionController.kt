/*
 * Copyright 2020 New Vector Ltd
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

package im.vector.riotx.features.crypto.verification.conclusion

import com.airbnb.epoxy.EpoxyController
import im.vector.riotx.R
import im.vector.riotx.core.epoxy.bottomsheet.bottomSheetSeparatorItem
import im.vector.riotx.core.resources.ColorProvider
import im.vector.riotx.core.resources.StringProvider
import im.vector.riotx.features.crypto.verification.epoxy.bottomSheetVerificationActionItem
import im.vector.riotx.features.crypto.verification.epoxy.bottomSheetVerificationBigImageItem
import im.vector.riotx.features.crypto.verification.epoxy.bottomSheetVerificationNoticeItem
import im.vector.riotx.features.html.EventHtmlRenderer
import javax.inject.Inject

class VerificationConclusionController @Inject constructor(
        private val stringProvider: StringProvider,
        private val colorProvider: ColorProvider,
        private val eventHtmlRenderer: EventHtmlRenderer
) : EpoxyController() {

    var listener: Listener? = null

    private var viewState: VerificationConclusionViewState? = null

    init {
        // We are requesting a model build directly as the first build of epoxy is on the main thread.
        // It avoids to build the whole list on the main thread.
        requestModelBuild()
    }

    fun update(viewState: VerificationConclusionViewState) {
        this.viewState = viewState
        requestModelBuild()
    }

    override fun buildModels() {
        val state = viewState ?: return

        when (state.conclusionState) {
            ConclusionState.SUCCESS -> {
                bottomSheetVerificationNoticeItem {
                    id("notice")
                    notice(stringProvider.getString(R.string.verification_conclusion_ok_notice))
                }

                bottomSheetVerificationBigImageItem {
                    id("image")
                    imageRes(R.drawable.ic_shield_trusted)
                }
            }
            ConclusionState.WARNING -> {
                bottomSheetVerificationNoticeItem {
                    id("notice")
                    notice(stringProvider.getString(R.string.verification_conclusion_not_secure))
                }

                bottomSheetVerificationBigImageItem {
                    id("image")
                    imageRes(R.drawable.ic_shield_warning)
                }

                bottomSheetVerificationNoticeItem {
                    id("warning_notice")
                    notice(eventHtmlRenderer.render(stringProvider.getString(R.string.verification_conclusion_compromised)))
                }
            }
            else                    -> Unit
        }

        bottomSheetSeparatorItem {
            id("sep0")
        }

        bottomSheetVerificationActionItem {
            id("done")
            title(stringProvider.getString(R.string.done))
            titleColor(colorProvider.getColorFromAttribute(R.attr.riotx_text_primary))
            iconRes(R.drawable.ic_arrow_right)
            iconColor(colorProvider.getColorFromAttribute(R.attr.riotx_text_primary))
            listener { listener?.onButtonTapped() }
        }
    }

    interface Listener {
        fun onButtonTapped()
    }
}
