package dev.nightfeather.its_mypic

import android.service.quicksettings.TileService

class TileService: TileService() {
    private val TAG: String = "ItsMyPicTile"

    override fun onTileAdded() {
        super.onTileAdded()
    }
    override fun onStartListening() {
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        if (Utils.checkOverlayPermission(this)) {
            Utils.startOverlayService(this, true);
        }
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }
}