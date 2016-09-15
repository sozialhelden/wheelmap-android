package org.wheelmap.android.tango.mode.operations;

import org.wheelmap.android.tango.renderer.WheelmapModeRenderer;

import java.util.Stack;

public abstract class OperationsModeRenderer extends WheelmapModeRenderer {

    private Stack<Operation> operationList = new Stack<>();

    public void addOperation(final Operation operation) {
        manipulateScene(new Consumer<Manipulator>() {
            @Override
            public void consume(Manipulator m) {
                operation.run(m);
                operationList.push(operation);
                onStatusHasChanged();
            }
        });
    }

    @Override
    public void undo() {
        manipulateScene(new Consumer<Manipulator>() {
            @Override
            public void consume(Manipulator m) {
                if (!operationList.empty()) {
                    operationList.pop().undo(m);
                }
                onStatusHasChanged();
            }
        });
    }
}
