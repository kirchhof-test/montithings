package components.body.subcomponents;

import types.TestTypes.Foo;

/*
 * Valid model. (in MontiArc 3)
 */
component UseEnumAsTypeArgFromCD {
    
    port in String sIn;
    
    component EnumFromCDAsTypeArg(Foo.Bar) sub;
    
    connect sIn -> sub.sIn;
}    