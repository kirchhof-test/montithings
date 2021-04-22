// -*- C++ -*-
// $Id$

/**
 * Code generated by the The ACE ORB (TAO) IDL Compiler v2.2a_p19
 * TAO and the TAO IDL Compiler have been developed by:
 *       Center for Distributed Object Computing
 *       Washington University
 *       St. Louis, MO
 *       USA
 *       http://www.cs.wustl.edu/~schmidt/doc-center.html
 * and
 *       Distributed Object Computing Laboratory
 *       University of California at Irvine
 *       Irvine, CA
 *       USA
 * and
 *       Institute for Software Integrated Systems
 *       Vanderbilt University
 *       Nashville, TN
 *       USA
 *       http://www.isis.vanderbilt.edu/
 *
 * Information about TAO is available at:
 *     http://www.cs.wustl.edu/~schmidt/TAO.html
 **/

// TAO_IDL - Generated from
// be/be_codegen.cpp:152

#ifndef _TAO_IDL_DDSRECORDERMESSAGEC_H_
#define _TAO_IDL_DDSRECORDERMESSAGEC_H_

#include /**/ "ace/pre.h"


#include /**/ "ace/config-all.h"

#if !defined (ACE_LACKS_PRAGMA_ONCE)
# pragma once
#endif /* ACE_LACKS_PRAGMA_ONCE */


#include "tao/ORB.h"
#include "tao/Basic_Types.h"
#include "tao/String_Manager_T.h"
#include "tao/VarOut_T.h"
#include "tao/Arg_Traits_T.h"
#include "tao/Basic_Arguments.h"
#include "tao/Special_Basic_Arguments.h"
#include "tao/Any_Insert_Policy_T.h"
#include "tao/Basic_Argument_T.h"
#include "tao/Fixed_Size_Argument_T.h"
#include "tao/Var_Size_Argument_T.h"
#include "tao/UB_String_Arguments.h"
#include /**/ "tao/Version.h"
#include /**/ "tao/Versioned_Namespace.h"

#if TAO_MAJOR_VERSION != 2 || TAO_MINOR_VERSION != 2 || TAO_BETA_VERSION != 0
#error This file should be regenerated with TAO_IDL
#endif

#if defined (TAO_EXPORT_MACRO)
#undef TAO_EXPORT_MACRO
#endif
#define TAO_EXPORT_MACRO 

// TAO_IDL - Generated from
// be/be_visitor_module/module_ch.cpp:38

namespace DDSRecorderMessage
{

  // TAO_IDL - Generated from
  // be/be_visitor_enum/enum_ch.cpp:47

  enum MessageType
  {
    MESSAGE_RECORD,
    INTERNAL_RECORDS,
    INTERNAL_STATE
  };

  typedef MessageType &MessageType_out;

  // TAO_IDL - Generated from
  // be/be_type.cpp:261

  struct Message;

  typedef
    ::TAO_Var_Var_T<
        Message
      >
    Message_var;

  typedef
    ::TAO_Out_T<
        Message
      >
    Message_out;

  // TAO_IDL - Generated from
  // be/be_visitor_structure/structure_ch.cpp:51

  struct  Message
  {

    // TAO_IDL - Generated from
    // be/be_type.cpp:307

    
    typedef Message_var _var_type;
    typedef Message_out _out_type;
    
    ::CORBA::Long id;
    ::TAO::String_Manager instance_name;
    DDSRecorderMessage::MessageType type;
    ::CORBA::LongLong timestamp;
    ::TAO::String_Manager serialized_vector_clock;
    ::TAO::String_Manager topic;
    ::CORBA::Long msg_id;
    ::TAO::String_Manager msg_content;
    ::TAO::String_Manager message_delays;
  };

  // TAO_IDL - Generated from
  // be/be_visitor_enum/enum_ch.cpp:47

  enum CommandType
  {
    RECORDING_START,
    RECORDING_STOP,
    SEND_INTERNAL_ND_CALLS
  };

  typedef CommandType &CommandType_out;

  // TAO_IDL - Generated from
  // be/be_type.cpp:261

  struct Command;

  typedef
    ::TAO_Var_Var_T<
        Command
      >
    Command_var;

  typedef
    ::TAO_Out_T<
        Command
      >
    Command_out;

  // TAO_IDL - Generated from
  // be/be_visitor_structure/structure_ch.cpp:51

  struct  Command
  {

    // TAO_IDL - Generated from
    // be/be_type.cpp:307

    
    typedef Command_var _var_type;
    typedef Command_out _out_type;
    
    ::TAO::String_Manager instance_name;
    DDSRecorderMessage::CommandType cmd;
  };

  // TAO_IDL - Generated from
  // be/be_type.cpp:261

  struct CommandReply;

  typedef
    ::TAO_Var_Var_T<
        CommandReply
      >
    CommandReply_var;

  typedef
    ::TAO_Out_T<
        CommandReply
      >
    CommandReply_out;

  // TAO_IDL - Generated from
  // be/be_visitor_structure/structure_ch.cpp:51

  struct  CommandReply
  {

    // TAO_IDL - Generated from
    // be/be_type.cpp:307

    
    typedef CommandReply_var _var_type;
    typedef CommandReply_out _out_type;
    
    ::TAO::String_Manager instance_name;
    ::CORBA::Long command_id;
    ::TAO::String_Manager content;
  };

  // TAO_IDL - Generated from
  // be/be_type.cpp:261

  struct Acknowledgement;

  typedef
    ::TAO_Var_Var_T<
        Acknowledgement
      >
    Acknowledgement_var;

  typedef
    ::TAO_Out_T<
        Acknowledgement
      >
    Acknowledgement_out;

  // TAO_IDL - Generated from
  // be/be_visitor_structure/structure_ch.cpp:51

  struct  Acknowledgement
  {

    // TAO_IDL - Generated from
    // be/be_type.cpp:307

    
    typedef Acknowledgement_var _var_type;
    typedef Acknowledgement_out _out_type;
    
    ::TAO::String_Manager sending_instance;
    ::TAO::String_Manager receiving_instance;
    ::TAO::String_Manager port_name;
    ::CORBA::Long acked_id;
    ::TAO::String_Manager serialized_vector_clock;
  };

// TAO_IDL - Generated from
// be/be_visitor_module/module_ch.cpp:67

} // module DDSRecorderMessage

// TAO_IDL - Generated from
// be/be_visitor_arg_traits.cpp:68

TAO_BEGIN_VERSIONED_NAMESPACE_DECL


// Arg traits specializations.
namespace TAO
{

  // TAO_IDL - Generated from
  // be/be_visitor_arg_traits.cpp:904

  template<>
  class Arg_Traits< ::DDSRecorderMessage::MessageType>
    : public
        Basic_Arg_Traits_T<
            ::DDSRecorderMessage::MessageType,
            TAO::Any_Insert_Policy_Noop
          >
  {
  };

  // TAO_IDL - Generated from
  // be/be_visitor_arg_traits.cpp:947

  template<>
  class Arg_Traits< ::DDSRecorderMessage::Message>
    : public
        Var_Size_Arg_Traits_T<
            ::DDSRecorderMessage::Message,
            TAO::Any_Insert_Policy_Noop
          >
  {
  };

  // TAO_IDL - Generated from
  // be/be_visitor_arg_traits.cpp:904

  template<>
  class Arg_Traits< ::DDSRecorderMessage::CommandType>
    : public
        Basic_Arg_Traits_T<
            ::DDSRecorderMessage::CommandType,
            TAO::Any_Insert_Policy_Noop
          >
  {
  };

  // TAO_IDL - Generated from
  // be/be_visitor_arg_traits.cpp:947

  template<>
  class Arg_Traits< ::DDSRecorderMessage::Command>
    : public
        Var_Size_Arg_Traits_T<
            ::DDSRecorderMessage::Command,
            TAO::Any_Insert_Policy_Noop
          >
  {
  };

  // TAO_IDL - Generated from
  // be/be_visitor_arg_traits.cpp:947

  template<>
  class Arg_Traits< ::DDSRecorderMessage::CommandReply>
    : public
        Var_Size_Arg_Traits_T<
            ::DDSRecorderMessage::CommandReply,
            TAO::Any_Insert_Policy_Noop
          >
  {
  };

  // TAO_IDL - Generated from
  // be/be_visitor_arg_traits.cpp:947

  template<>
  class Arg_Traits< ::DDSRecorderMessage::Acknowledgement>
    : public
        Var_Size_Arg_Traits_T<
            ::DDSRecorderMessage::Acknowledgement,
            TAO::Any_Insert_Policy_Noop
          >
  {
  };
}

TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_visitor_traits.cpp:62

TAO_BEGIN_VERSIONED_NAMESPACE_DECL

// Traits specializations.
namespace TAO
{
}
TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_visitor_enum/cdr_op_ch.cpp:37


TAO_BEGIN_VERSIONED_NAMESPACE_DECL

 ::CORBA::Boolean operator<< (TAO_OutputCDR &strm, DDSRecorderMessage::MessageType _tao_enumerator);
 ::CORBA::Boolean operator>> (TAO_InputCDR &strm, DDSRecorderMessage::MessageType &_tao_enumerator);

TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_visitor_structure/cdr_op_ch.cpp:46


TAO_BEGIN_VERSIONED_NAMESPACE_DECL

 ::CORBA::Boolean operator<< (TAO_OutputCDR &, const DDSRecorderMessage::Message &);
 ::CORBA::Boolean operator>> (TAO_InputCDR &, DDSRecorderMessage::Message &);

TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_visitor_enum/cdr_op_ch.cpp:37


TAO_BEGIN_VERSIONED_NAMESPACE_DECL

 ::CORBA::Boolean operator<< (TAO_OutputCDR &strm, DDSRecorderMessage::CommandType _tao_enumerator);
 ::CORBA::Boolean operator>> (TAO_InputCDR &strm, DDSRecorderMessage::CommandType &_tao_enumerator);

TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_visitor_structure/cdr_op_ch.cpp:46


TAO_BEGIN_VERSIONED_NAMESPACE_DECL

 ::CORBA::Boolean operator<< (TAO_OutputCDR &, const DDSRecorderMessage::Command &);
 ::CORBA::Boolean operator>> (TAO_InputCDR &, DDSRecorderMessage::Command &);

TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_visitor_structure/cdr_op_ch.cpp:46


TAO_BEGIN_VERSIONED_NAMESPACE_DECL

 ::CORBA::Boolean operator<< (TAO_OutputCDR &, const DDSRecorderMessage::CommandReply &);
 ::CORBA::Boolean operator>> (TAO_InputCDR &, DDSRecorderMessage::CommandReply &);

TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_visitor_structure/cdr_op_ch.cpp:46


TAO_BEGIN_VERSIONED_NAMESPACE_DECL

 ::CORBA::Boolean operator<< (TAO_OutputCDR &, const DDSRecorderMessage::Acknowledgement &);
 ::CORBA::Boolean operator>> (TAO_InputCDR &, DDSRecorderMessage::Acknowledgement &);

TAO_END_VERSIONED_NAMESPACE_DECL



// TAO_IDL - Generated from
// be/be_codegen.cpp:1703
#if defined (__ACE_INLINE__)
#include "DDSRecorderMessageC.inl"
#endif /* defined INLINE */

#include /**/ "ace/post.h"

#endif /* ifndef */

