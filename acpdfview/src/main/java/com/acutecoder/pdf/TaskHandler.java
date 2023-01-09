package com.acutecoder.pdf;

/*
 *Created by Bhuvaneshwaran
 *on 9:07 PM, 1/8/2023
 *AcuteCoder
 */

import java.util.ArrayList;

@SuppressWarnings("all")
final class TaskHandler extends ArrayList<Task> {

    private final ArrayList<Integer> positions;
    private boolean isRunning = false;

    TaskHandler() {
        positions = new ArrayList<>();
    }

    @Override
    public boolean add(Task tTask) {
        if (positions.contains(tTask.getPosition()))
            return false;
        tTask.setOnStateListener(new Task.OnStateListener() {

            @Override
            public void onStart(Task task) {
                isRunning = true;
            }

            @Override
            public void onComplete(Task task) {
                isRunning = false;
                remove(task);
                positions.remove(Integer.valueOf(task.getPosition()));
                startAvailableTask();
            }
        });
        positions.add(tTask.getPosition());
        return super.add(tTask);
    }

    boolean isRunning() {
        return isRunning;
    }

    void startAvailableTask() {
        if (size() > 0)
            get(0).start();
    }
}
