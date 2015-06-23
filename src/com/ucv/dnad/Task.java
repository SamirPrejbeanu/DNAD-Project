package com.ucv.dnad;

/**
 * Created by S on 6/22/2015.
 */
public class Task {

    private String taskName;
    private String taskPath;
    private int complexityLevel;
    private boolean status = false;

    public Task(){}

    public Task(String taskName,String taskPath,int complexityLevel){
        this.taskName = taskName;
        this.taskPath = taskPath;
        this.complexityLevel = complexityLevel;
    }

    public String getTaskName(){
        return this.taskName;
    }

    public String getTaskPath(){
        return this.taskPath;
    }

    public int getComplexityLevel(){
        return this.complexityLevel;
    }

    public void taskResolved(){
        this.status = true;
    }

    public boolean getStatus(){
        return this.status;
    }
}
