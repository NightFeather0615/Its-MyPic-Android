package dev.nightfeather.its_mypic

import android.service.quicksettings.TileService

class TileService: TileService() {
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
        if (Utils.Permission.checkOverlayPermission(this)) {
            Utils.Overlay.startService(this, true);
        }
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }
}
