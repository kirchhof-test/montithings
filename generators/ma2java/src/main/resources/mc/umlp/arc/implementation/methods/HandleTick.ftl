${tc.params("de.monticore.lang.montiarc.montiarc._symboltable.ComponentSymbol compSym", "de.montiarc.generator.codegen.GeneratorHelper helper")}

<#if compSym.isAtomic()>
    /* (non-Javadoc)
     * @see ${glex.getGlobalVar("AComponent")}#handleTick()
     */
    @Override
    public void handleTick() {
        <#-- ${op.includeTemplates(handleTickStartHook, ast)}-->
    <#if glex.getGlobalVar("TIME_PARADIGM_STORAGE_KEY").isTimeSynchronous()>
        triggerTimeSync();
    </#if>    
    <#list compSym.getOutgoingPorts() as port>
        ${_templates.mc.umlp.arc.implementation.methods.HandleTickPorts.generate(helper.printType(port.getTypeReference()), port.getName())}
    </#list>
    <#if compSym.getSuperComponent().isPresent()>
        super.handleTick();
    <#elseif glex.getGlobalVar("TIME_PARADIGM_STORAGE_KEY").isTimed()>
        incLocalTime();
        timeStep();
    </#if>
        <#-- ${op.includeTemplates(handleTickEndHook, ast)} -->
    }
    
</#if>