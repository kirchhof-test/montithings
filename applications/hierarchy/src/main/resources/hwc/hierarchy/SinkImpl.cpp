#include "SinkImpl.h"
#include <iostream>

SinkResult
SinkImpl::getInitialValues ()
{
  return {};
}

SinkResult
SinkImpl::compute (SinkInput input)
{
  if (input.getValue ())
    {
      std::cout << input.getValue ().value () << std::endl;
    }
  else
    { std::cout << "No data." << std::endl; }
  return {};
}
