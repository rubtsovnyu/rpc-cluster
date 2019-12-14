#pragma once
#include <charconv>
#if defined(__GNUC__)

inline std::from_chars_result FromCharsForStupidGcc(const char* const begin, const char* const end, double& value)
{
    int32_t tempInt = 0;
    auto convertResult = std::from_chars(begin, end, tempInt);
    if (*convertResult.ptr != '.' || convertResult.ec != std::errc())
    {
        value = tempInt;
        return convertResult;
    }
    uint32_t tempUint;
    const auto tailBegin = convertResult.ptr+1;
    convertResult = std::from_chars(tailBegin, end, tempUint);
    if (convertResult.ec != std::errc())
        return convertResult;
    value = tempInt + static_cast<double>(tempUint) / (convertResult.ptr - tailBegin);
    return convertResult;
}

namespace std
{
    inline std::from_chars_result from_chars(const char* const begin, const char* const end, double& value)
    {
        return FromCharsForStupidGcc(begin, end, value);
    }
}

#endif