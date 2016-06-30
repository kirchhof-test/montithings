package sim.generic;

import sim.IScheduler;
import sim.error.ISimulationErrorHandler;

/**
 * Represents an abstract component in the simulation,
 * 
 * <br>
 * <br>
 * Copyright (c) 2010 RWTH Aachen. All rights reserved.
 * 
 * @author Arne Haber
 * @version 13.10.2008
 */
public abstract class AComponent implements ISimComponent {
    
    /** Handles ArcSimProblemReports. */
    private ISimulationErrorHandler errorHandler;

    /** Name of this component. */
    private String componentName;
    
    /** Scheduler of this component. */
    private IScheduler scheduler;
    
    /** Id assigned by the scheduler. */
    private int id = -1;

    /**
     * @return the scheduler
     */
    protected IScheduler getScheduler() {
        return scheduler;
    }

    /**
     * @param scheduler the scheduler to set
     */
    protected void setScheduler(IScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Creates a new {@link AComponent}.
     */
    public AComponent() {
        super();
    }

    /**
     * @return the errorHandler
     */
    @Override
    public ISimulationErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    
    /**
     * @param errorHandler the errorHandler to set
     */
    protected void setErrorHandler(ISimulationErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    /*
     * (non-Javadoc)
     * @see sim.generic.IComponent#setName(java.lang.String)
     */
    @Override
    public void setComponentName(String name) {
        this.componentName = name;
    }
    
    /* (non-Javadoc)
     * @see sim.generic.IComponent#getName()
     */
    @Override
    public String getComponentName() {
        return this.componentName;
    }

    @Override
    public String toString() {
        if (getComponentName() != null) {
            return getComponentName();
        }
        else {
            return this.getClass().getName();
        }
    }

    /**
     * @see sim.generic.ISimComponent#setSimulationId(int)
     */
    @Override
    public final void setSimulationId(int id) {
        this.id = id;
    }

    /**
     * @see sim.generic.ISimComponent#getSimulationId()
     */
    @Override
    public final int getSimulationId() {
        return this.id;
    }
    
}
