<#-- (c) https://github.com/MontiCore/monticore -->
${tc.signature("comp","config")}
<#include "/template/component/helper/GeneralPreamble.ftl">


<#list comp.subComponents as subcomponent>
    ${subcomponent.getName()}( instanceName + ".${subcomponent.getName()}"
    <#if config.getMessageBroker().toString() == "MQTT">
    , passedMqttClientInstance
    , passedMqttClientLocalInstance
    </#if>
    <#if config.getSplittingMode().toString() == "OFF" || ComponentHelper.shouldIncludeSubcomponents(comp, config)>
        <#list ComponentHelper.getParamValues(subcomponent) as param >
            <#if param?index==0>,</#if>
            ${param}<#sep>,</#sep>
        </#list>
    </#if>)<#sep>,</#sep>
</#list>