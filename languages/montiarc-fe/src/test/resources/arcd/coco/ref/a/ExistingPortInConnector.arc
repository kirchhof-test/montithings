package a;

import b.CorrectCompInB;

component ExistingPortInConnector {
    port 
        in String strIn,
        out String strOut1,
        out String strOut2;
        
    component CorrectCompInA ccia [stringOutWrong -> strOut1];
    
    component CorrectCompInB ccib;
    
    connect strIn -> ccib.stringInWrong, ccia.stringIn;
    
    connect ccib.stringOutWrong -> strOut2;
}