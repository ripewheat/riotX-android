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

package im.vector.riotredesign.features.home.room.detail.timeline.helper

import im.vector.matrix.android.api.session.room.model.Membership
import im.vector.matrix.android.api.session.room.model.RoomMember
import im.vector.matrix.android.api.session.room.timeline.TimelineEvent

object RoomMemberEventHelper {

    fun senderAvatar(eventContent: RoomMember?, prevEventContent: RoomMember?, event: TimelineEvent): String? {
        return if (eventContent?.membership == Membership.LEAVE && eventContent.avatarUrl == null && prevEventContent?.avatarUrl != null) {
            prevEventContent.avatarUrl
        } else {
            event.senderAvatar
        }
    }

    fun senderName(eventContent: RoomMember?, prevEventContent: RoomMember?, event: TimelineEvent): String? {
        return if (eventContent?.membership == Membership.LEAVE && eventContent.displayName == null && prevEventContent?.displayName != null) {
            prevEventContent.displayName
        } else {
            event.senderName
        }
    }
}