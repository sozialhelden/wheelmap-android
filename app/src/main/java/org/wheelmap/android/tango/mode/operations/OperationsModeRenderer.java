package org.wheelmap.android.tango.mode.operations;

import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

import java.util.Stack;

public abstract class OperationsModeRenderer extends WheelmapModeRenderer {

    private Stack<Operation> operationList = new Stack<>();

    public void addOperation(Operation operation) {
        manipulateScene(m -> {
            operation.run(m);
            operationList.push(operation);
        });
    }

    @Override
    public void undo() {
        manipulateScene(m -> {
            if (!operationList.empty()) {
                operationList.pop().undo(m);
            }
        });
    }
}
