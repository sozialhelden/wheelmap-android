package org.wheelmap.android.tango.mode.operations;

import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

import java.util.Stack;

public abstract class OperationsModeRenderer extends WheelmapModeRenderer {

    private Stack<Operation> operationList = new Stack<>();

    private boolean isOperationRunning = false;

    public void addOperation(final Operation operation) {
        isOperationRunning = true;
        manipulateScene(new Consumer<Manipulator>() {
            @Override
            public void consume(Manipulator m) {
                operation.run(m);
                operationList.push(operation);
                isOperationRunning = false;
                onStatusHasChanged();
            }
        });
    }

    @Override
    public void undo() {
        isOperationRunning = true;
        manipulateScene(new Consumer<Manipulator>() {
            @Override
            public void consume(Manipulator m) {
                if (!operationList.empty()) {
                    operationList.pop().undo(m);
                }
                isOperationRunning = false;
                onStatusHasChanged();
            }
        });
    }

    /**
     * checks if there are any operations in the queue to perform in the next iteration
     */
    protected boolean isOperationRunning() {
        return isOperationRunning;
    }
}
