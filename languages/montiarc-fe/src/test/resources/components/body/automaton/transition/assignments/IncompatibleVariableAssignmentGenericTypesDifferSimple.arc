package components.body.automaton.transition.assignments;

import types.Datatypes.MotorCommand;
import types.Datatypes.TimerSignal;
import types.Datatypes.TimerCmd;
import java.util.HashMap;

/*
 * Invalid model.
 *
 * @implements TODO
 */
component IncompatibleVariableAssignmentGenericTypesDifferSimple {
  
  HashMap<String, Integer> stateChanges;

  automaton BumpControl {
    state Idle;
    initial Idle / {stateChanges.put("foo", 5)};
    Idle -> Idle / {stateChanges.put("asd", 4)};
  }
}