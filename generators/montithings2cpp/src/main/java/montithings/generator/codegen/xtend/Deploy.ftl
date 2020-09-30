# (c) https://github.com/MontiCore/monticore
<#--package montithings.generator.codegen.xtend

import arcbasis._symboltable.ComponentTypeSymbol
import montithings.generator.helper.ComponentHelper
import montithings.generator.codegen.ConfigParams-->

class Deploy {
  
  def static generateDeploy(ComponentTypeSymbol comp, String compname, ConfigParams config) {
    return '''
    #include "${compname}.h"
    <#if config.getSplittingMode() != ConfigParams.SplittingMode.OFF>
 #include "${compname}Manager.h"
 </#if>
    #include <chrono>
    #include <thread>

    int main(int argc, char* argv[])
    {
      <#if config.getSplittingMode() == ConfigParams.SplittingMode.LOCAL>
      if (argc != 4)
      {
        std::cerr << "Called with wrong number of arguments. Please provide the following arguments:" << std::endl;
        std::cerr << "1) The component's instance name" << std::endl;
        std::cerr << "2) Network port for management traffic" << std::endl;
        std::cerr << "3) Network port for data traffic" << std::endl;
        std::cerr << std::endl;
        std::cerr << "Aborting." << std::endl;
        exit(1);
      }
      </#if>
      <#if config.getSplittingMode() != ConfigParams.SplittingMode.LOCAL>
      if (argc != 2)
      {
        std::cerr << "Called with wrong number of arguments. Please provide the following arguments:" << std::endl;
        std::cerr << "1) The component's instance name" << std::endl;
        std::cerr << std::endl;
        std::cerr << "Aborting." << std::endl;
        exit(1);
      }
      </#if>

      ${ComponentHelper.printPackageNamespaceForComponent(comp)}${compname} cmp (argv[1]);
      <#if config.getSplittingMode() != ConfigParams.SplittingMode.OFF>
      ${ComponentHelper.printPackageNamespaceForComponent(comp)}${compname}Manager manager (&cmp, argv[2], argv[3]);
      manager.initializePorts ();
      <#if comp.isDecomposed>
 manager.searchSubcomponents ();
 </#if>
      </#if>

      cmp.setUp(<#if ComponentHelper.isTimesync(comp)>
 TIMESYNC
 <#else>
 EVENTBASED
  </#if>);
      cmp.init();
      <#if !ComponentHelper.isTimesync(comp)>
 cmp.start();
 </#if>

      
      std::cout << "Started." << std::endl;
    
      while (true)
      {
        auto end = std::chrono::high_resolution_clock::now() + ${ComponentHelper.getExecutionIntervalMethod(comp)};
        <#if ComponentHelper.isTimesync(comp)>
 cmp.compute();
 </#if>
        do {
          std::this_thread::yield();
          <#if ComponentHelper.isTimesync(comp)>
 std::this_thread::sleep_for(std::chrono::milliseconds(1));
 <#else>
 std::this_thread::sleep_for(std::chrono::milliseconds(1000));
  </#if>
          } while (std::chrono::high_resolution_clock::now()  < end);
        }
        return 0;
      }
    '''
  }

  
  def static generateDeployArduino(ComponentTypeSymbol comp, String compname) {
    return '''
    #include "${compname}.h"
    
    ${ComponentHelper.printPackageNamespaceForComponent(comp)}${compname} cmp;
    const long interval = ${ComponentHelper.getExecutionIntervalInMillis(comp)};
    unsigned long previousMillis = 0;
    
    void setup() {
      Serial.begin(9600);
      cmp.setUp(<#if ComponentHelper.isTimesync(comp)>
 TIMESYNC
 <#else>
 EVENTBASED
  </#if>);
      cmp.init();
      <#if !ComponentHelper.isTimesync(comp)>
 cmp.start();
 </#if>
    }
    
    void loop() {
      <#if ComponentHelper.isTimesync(comp)>
      unsigned long currentMillis = millis();

      if (currentMillis >= previousMillis + interval) {
        previousMillis = currentMillis;
        cmp.compute();
      }
      </#if>
    }
    '''
  }
}