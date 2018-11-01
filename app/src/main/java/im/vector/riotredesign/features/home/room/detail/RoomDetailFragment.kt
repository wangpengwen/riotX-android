package im.vector.riotredesign.features.home.room.detail

import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.RequestOptions
import im.vector.matrix.android.api.Matrix
import im.vector.matrix.android.api.session.events.model.EnrichedEvent
import im.vector.matrix.android.api.session.room.Room
import im.vector.matrix.android.api.session.room.model.RoomSummary
import im.vector.riotredesign.R
import im.vector.riotredesign.core.extensions.avatarDrawable
import im.vector.riotredesign.core.glide.GlideApp
import im.vector.riotredesign.core.platform.RiotFragment
import im.vector.riotredesign.core.platform.ToolbarConfigurable
import im.vector.riotredesign.core.utils.Constants
import im.vector.riotredesign.core.utils.FragmentArgumentDelegate
import kotlinx.android.synthetic.main.fragment_room_detail.*
import org.koin.android.ext.android.inject

class RoomDetailFragment : RiotFragment() {

    companion object {

        fun newInstance(roomId: String): RoomDetailFragment {
            return RoomDetailFragment().apply {
                this.roomId = roomId
            }
        }
    }

    private val matrix by inject<Matrix>()
    private val currentSession = matrix.currentSession
    private var roomId by FragmentArgumentDelegate<String>()
    private lateinit var timelineEventController: TimelineEventController
    private lateinit var room: Room

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_room_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        room = currentSession.getRoom(roomId)!!
        setupRecyclerView()
        setupToolbar()
        room.loadRoomMembersIfNeeded()
        room.liveTimeline().observe(this, Observer { renderEvents(it) })
        room.roomSummary.observe(this, Observer { renderRoomSummary(it) })
    }

    private fun setupToolbar() {
        val parentActivity = riotActivity
        if (parentActivity is ToolbarConfigurable) {
            parentActivity.configure(toolbar)
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        recyclerView.layoutManager = layoutManager
        timelineEventController = TimelineEventController(riotActivity)
        recyclerView.setController(timelineEventController)
    }

    private fun renderRoomSummary(roomSummary: RoomSummary?) {
        roomSummary?.let {
            toolbarTitleView.text = it.displayName
            val avatarUrl = it.avatarUrl.replace("mxc://", Constants.MEDIA_URL)
            GlideApp
                    .with(this)
                    .load(avatarUrl)
                    .placeholder(riotActivity.avatarDrawable(it.displayName))
                    .apply(RequestOptions.circleCropTransform())
                    .into(toolbarAvatarImageView)
            if (it.topic.isNotEmpty()) {
                toolbarSubtitleView.visibility = View.VISIBLE
                toolbarSubtitleView.text = it.topic
            } else {
                toolbarSubtitleView.visibility = View.GONE
            }
        }
    }

    private fun renderEvents(events: PagedList<EnrichedEvent>?) {
        timelineEventController.timeline = events
        timelineEventController.requestModelBuild()
    }

}