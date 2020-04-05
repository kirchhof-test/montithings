// (c) https://github.com/MontiCore/monticore
package montithings.generator.codegen.xtend.util

class ArduinoReadme {
  def static printArduinoReadme(String pack, String compname) {
  	return '''
  	How to compile for Arduino-based boards:
  	
  	1) Copy the folder «pack» to <Arduino Folder>/libraries/
  	2) Copy the folder montithings-RTE to <Arduino Folder>/libraries/
  	3) Copy the folder Deploy«compname» to <Arduino Folder>/
  	4) Open the <Arduino Folder>/Deploy«compname»/Deploy«compname».ino using the Arduino app
  	5) Connect your board and click "Upload"
  	'''
  }
}