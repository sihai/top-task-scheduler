package com.ihome.top.scheduler.exception;

public class JobExecutionException extends SchedulerException
{
	/*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private boolean refire = false;

    private boolean unscheduleTrigg = false;

    private boolean unscheduleAllTriggs = false;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a JobExcecutionException, with the 're-fire immediately' flag set
     * to <code>false</code>.
     * </p>
     */
    public JobExecutionException() {
    }

    /**
     * <p>
     * Create a JobExcecutionException, with the given cause.
     * </p>
     */
    public JobExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>
     * Create a JobExcecutionException, with the given message.
     * </p>
     */
    public JobExecutionException(String msg) {
        super(msg);
    }

    /**
     * <p>
     * Create a JobExcecutionException with the 're-fire immediately' flag set
     * to the given value.
     * </p>
     */
    public JobExecutionException(boolean refireImmediately) {
        refire = refireImmediately;
    }

    /**
     * <p>
     * Create a JobExcecutionException with the given underlying exception, and
     * the 're-fire immediately' flag set to the given value.
     * </p>
     */
    public JobExecutionException(Throwable cause, boolean refireImmediately) {
        super(cause);

        refire = refireImmediately;
    }

    /**
     * <p>
     * Create a JobExcecutionException with the given message, and underlying
     * exception.
     * </p>
     */
    public JobExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    /**
     * <p>
     * Create a JobExcecutionException with the given message, and underlying
     * exception, and the 're-fire immediately' flag set to the given value.
     * </p>
     */
    public JobExecutionException(String msg, Throwable cause,
            boolean refireImmediately) {
        super(msg, cause);

        refire = refireImmediately;
    }
    
    /**
     * Create a JobExcecutionException with the given message and the 're-fire 
     * immediately' flag set to the given value.
     */
    public JobExecutionException(String msg, boolean refireImmediately) {
        super(msg);

        refire = refireImmediately;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void setRefireImmediately(boolean refire) {
        this.refire = refire;
    }

    public boolean refireImmediately() {
        return refire;
    }

    public void setUnscheduleFiringTrigger(boolean unscheduleTrigg) {
        this.unscheduleTrigg = unscheduleTrigg;
    }

    public boolean unscheduleFiringTrigger() {
        return unscheduleTrigg;
    }

    public void setUnscheduleAllTriggers(boolean unscheduleAllTriggs) {
        this.unscheduleAllTriggs = unscheduleAllTriggs;
    }

    public boolean unscheduleAllTriggers() {
        return unscheduleAllTriggs;
    }
}
