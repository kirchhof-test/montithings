// (c) https://github.com/MontiCore/monticore
package hierarchy;

import Colors.*;

component Source {
  port out Colors.Color value;

  // calls hand-written compute method every second
  update interval 1s;
}
