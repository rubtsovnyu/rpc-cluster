get_filename_component(target_name ${CMAKE_CURRENT_SOURCE_DIR} NAME)

if(MSVC)
  add_definitions(-D_WIN32_WINNT=0x600)
endif()

set(protobuf_MODULE_COMPATIBLE TRUE)
find_package(protobuf REQUIRED)
message(STATUS "Using protobuf ${protobuf_VERSION}")

find_package(gRPC CONFIG REQUIRED)
message(STATUS "Using gRPC ${gRPC_VERSION}")

find_package(Boost COMPONENTS filesystem REQUIRED)

set(gRPC_CPP_PLUGIN_EXECUTABLE $<TARGET_FILE:gRPC::grpc_cpp_plugin>)

get_filename_component(hw_proto "../cluster.proto" ABSOLUTE)
get_filename_component(hw_proto_path "${hw_proto}" PATH)

protobuf_generate_cpp(hw_proto_srcs hw_proto_hdrs "${hw_proto}")
set(hw_grpc_srcs "${CMAKE_CURRENT_BINARY_DIR}/cluster.grpc.pb.cc")
set(hw_grpc_hdrs "${CMAKE_CURRENT_BINARY_DIR}/cluster.grpc.pb.h")
add_custom_command(
      OUTPUT "${hw_grpc_srcs}" "${hw_grpc_hdrs}"
      COMMAND protobuf::protoc
      ARGS --grpc_out "${CMAKE_CURRENT_BINARY_DIR}" -I "${hw_proto_path}"
        --plugin=protoc-gen-grpc="${gRPC_CPP_PLUGIN_EXECUTABLE}"
        "${hw_proto}"
      DEPENDS "${hw_proto}")

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src ${CMAKE_CURRENT_BINARY_DIR} ${Boost_INCLUDE_DIR})

if (WIN32)
	message(STATUS "Using Windows native sources")
	set(NATIVE_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/src/native/windows/*.cpp)
else()
	message(STATUS "Using Posix native sources")
	set(NATIVE_SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/src/native/posix/*.cpp)
endif()

file(GLOB sources 
  ${CMAKE_CURRENT_SOURCE_DIR}/src/*.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/*.h
	${NATIVE_SOURCES}
)

add_executable(${target_name} ${sources}
	${hw_proto}
	${hw_proto_srcs}
	${hw_proto_hdrs}
	${hw_grpc_srcs}
	${hw_grpc_hdrs})
target_link_libraries(${target_name}
	protobuf::libprotobuf
	gRPC::grpc++_unsecure
	${Boost_LIBRARIES})
	
set_target_properties(${target_name} PROPERTIES CXX_STANDARD 17)
