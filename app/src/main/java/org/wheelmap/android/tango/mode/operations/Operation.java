package org.wheelmap.android.tango.mode.operations;

import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

public interface Operation {
    void run(WheelmapModeRenderer.Manipulator manipulator);
    void undo(WheelmapModeRenderer.Manipulator manipulator);
}
