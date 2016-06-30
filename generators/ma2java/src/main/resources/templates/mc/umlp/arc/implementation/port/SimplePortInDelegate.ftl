${tc.params("de.monticore.lang.montiarc.montiarc._symboltable.PortSymbol portSym", "boolean singleIn", "de.montiarc.generator.codegen.GeneratorHelper helper")}


<#if singleIn>
    /* (non-Javadoc)
     * @see ${glex.getGlobalValue("SimpleInPortInterface")}#messageReceived(sim.generic.Message)
     */
    @Override
    public void messageReceived(${helper.printType(portSym.getTypeReference())} message) {
    <#if !glex.getGlobalValue("TIME_PARADIGM_STORAGE_KEY").isTimeSynchronous()>
        treat${portSym.getName()?cap_first}(message);
    </#if>
    }
</#if>