<!-- (c) https://github.com/MontiCore/monticore -->
# Sensor & Actuator Access

This example shows how to connect components to sensors and actuators or, more
generally speaking, hardware.
The example is based on the basic-input-output example but has two additional
ports:

<img src="../../docs/SensorActuatorAccess.png" alt="drawing" height="200px"/>

The additional unconnected ports can be used for accessing hardware such as 
sensors and actuators.
To do so, developers can provide Freemarker templates in the hand-written code 
folder. 
MontiThings uses the templates to inject code in the ports.
To enable MontiThings to find the templates, you have to give them specific 
names and replace `<ComponentName>` and `<PortName>` with the names of the 
component and port you want to inject your code into: 
- To add import statements, use `<ComponentName><PortName>Include.ftl`
- To provide values to the component (i.e. implement an incoming port), 
use `<ComponentName><PortName>Provide.ftl`
- To process values from the component (i.e. implement an outgoing port), 
use `<ComponentName><PortName>Consume.ftl`
- To specify the type of the port use `<ComponentName><PortName>Type.ftl`
- To provide the topic which the port publishes or subscribes to use
`<ComponentName><PortName>Topic.ftl`

For example, we can implement an actuator port like this: 
For the purpose of not requiring any hardware to run this example, we just print
the values to the console.
This is done by `TestActuatorConsume.ftl`:
```
if (nextVal)
  {
    std::cout << "Sink: " << nextVal.value () << std::endl;
  }
else
  { 
    std::cout << "Sink: " << "No data." << std::endl; 
  }
```
If there's a value at the port it prints "Sink: " followed by the value at the 
port. 
Otherwise (if the compute method does not write data to that port), we only 
print "Sink: No data.".
As this uses `std::cout` our C++ code also requires an include statement which
we add with the `TestActuatorInclude.ftl` template:
```
#include <iostream>
```

Accordingly, running this example will result in both of the actuators printing
increasing numbers:
```
Sink: 0
Sink: 1
Sink: 2
Sink: 3
Sink: 4
Sink: 5
Sink: 6
```