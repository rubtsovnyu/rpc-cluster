cmake_minimum_required(VERSION 2.8)

project(cluster CXX)

if(NOT MSVC)
  set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17")
else()
  add_definitions(-D_WIN32_WINNT=0x600)
endif()

set(protobuf_MODULE_COMPATIBLE TRUE)
find_package(protobuf REQUIRED)
message(STATUS "Using protobuf ${protobuf_VERSION}")

find_package(gRPC CONFIG REQUIRED)
message(STATUS "Using gRPC ${gRPC_VERSION}")

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

include_directories(${PROJECT_SOURCE_DIR}/src ${CMAKE_CURRENT_BINARY_DIR})

file(GLOB sources 
    ${PROJECT_SOURCE_DIR}/src/ControlServiceImpl.cpp
    ${PROJECT_SOURCE_DIR}/src/RpcPointsStream.cpp
    ${PROJECT_SOURCE_DIR}/src/RpcServer.cpp
    ${PROJECT_SOURCE_DIR}/src/StartTaskCall.cpp
    ${PROJECT_SOURCE_DIR}/src/StubTaskManager.cpp
    ${PROJECT_SOURCE_DIR}/src/cluster.cpp
${PROJECT_SOURCE_DIR}/src/*.h)

add_executable(cluster ${sources}
	${hw_proto}
	${hw_proto_srcs}
	${hw_proto_hdrs}
	${hw_grpc_srcs}
	${hw_grpc_hdrs})
target_link_libraries(cluster
	protobuf::libprotobuf
	gRPC::grpc++_unsecure)
