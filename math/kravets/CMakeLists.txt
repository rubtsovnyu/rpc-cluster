get_filename_component(target_name ${CMAKE_CURRENT_SOURCE_DIR} NAME)
add_executable("math.${target_name}" main.cpp)