@file:Suppress("unused")

package com.epam.drill.api

import com.epam.drill.core.messanger.MessageQueue
import com.epam.drill.core.request.DrillRequest
import com.epam.drill.jvmapi.JNI
import com.epam.drill.jvmapi.jni
import com.epam.drill.plugin.PluginManager
import com.epam.drill.plugin.api.processing.AgentPart
import com.epam.drill.plugin.api.processing.NativePart
import jvmapi.GetCurrentThread
import jvmapi.GetThreadLocalStorage
import jvmapi.JVMTI_DISABLE
import jvmapi.JVMTI_ENABLE
import jvmapi.JVMTI_EVENT_BREAKPOINT
import jvmapi.JVMTI_EVENT_CLASS_FILE_LOAD_HOOK
import jvmapi.JVMTI_EVENT_CLASS_LOAD
import jvmapi.JVMTI_EVENT_CLASS_PREPARE
import jvmapi.JVMTI_EVENT_COMPILED_METHOD_LOAD
import jvmapi.JVMTI_EVENT_COMPILED_METHOD_UNLOAD
import jvmapi.JVMTI_EVENT_DATA_DUMP_REQUEST
import jvmapi.JVMTI_EVENT_DYNAMIC_CODE_GENERATED
import jvmapi.JVMTI_EVENT_EXCEPTION
import jvmapi.JVMTI_EVENT_EXCEPTION_CATCH
import jvmapi.JVMTI_EVENT_FIELD_ACCESS
import jvmapi.JVMTI_EVENT_FIELD_MODIFICATION
import jvmapi.JVMTI_EVENT_FRAME_POP
import jvmapi.JVMTI_EVENT_GARBAGE_COLLECTION_FINISH
import jvmapi.JVMTI_EVENT_GARBAGE_COLLECTION_START
import jvmapi.JVMTI_EVENT_METHOD_ENTRY
import jvmapi.JVMTI_EVENT_METHOD_EXIT
import jvmapi.JVMTI_EVENT_MONITOR_CONTENDED_ENTER
import jvmapi.JVMTI_EVENT_MONITOR_CONTENDED_ENTERED
import jvmapi.JVMTI_EVENT_MONITOR_WAIT
import jvmapi.JVMTI_EVENT_MONITOR_WAITED
import jvmapi.JVMTI_EVENT_NATIVE_METHOD_BIND
import jvmapi.JVMTI_EVENT_OBJECT_FREE
import jvmapi.JVMTI_EVENT_RESOURCE_EXHAUSTED
import jvmapi.JVMTI_EVENT_SINGLE_STEP
import jvmapi.JVMTI_EVENT_THREAD_END
import jvmapi.JVMTI_EVENT_THREAD_START
import jvmapi.JVMTI_EVENT_VM_DEATH
import jvmapi.JVMTI_EVENT_VM_INIT
import jvmapi.JVMTI_EVENT_VM_OBJECT_ALLOC
import jvmapi.JVMTI_EVENT_VM_START
import jvmapi.SetEventCallbacks
import jvmapi.SetEventNotificationMode
import jvmapi.gdata
import jvmapi.gjavaVMGlob
import jvmapi.jthread
import jvmapi.jthreadVar
import jvmapi.jvmtiEnvVar
import jvmapi.jvmtiEventCallbacks
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value

/**
 * we should duplicate all of this methods signature with "external" keyword and "@SymbolName" annotation, without body,
 * in order to allow use this API for native plugins
 */

@CName("JNIFun")
fun JNIFun(): JNI {
    return jni
}

@CName("JNIEn")
fun JNIEn(): JNI {
    return jni
}

//
//@CName("GetClassSignature")
//fun GetClassSignature(
//    klass: jvmapi.jclass?,
//    signature_ptr: kotlinx.cinterop.CValuesRef<kotlinx.cinterop.CPointerVar<kotlinx.cinterop.ByteVar /* = kotlinx.cinterop.ByteVarOf<kotlin.Byte> */> /* = kotlinx.cinterop.CPointerVarOf<kotlinx.cinterop.CPointer<kotlinx.cinterop.ByteVarOf<kotlin.Byte>>> */>?,
//    generic_ptr: kotlinx.cinterop.CValuesRef<kotlinx.cinterop.CPointerVar<kotlinx.cinterop.ByteVar /* = kotlinx.cinterop.ByteVarOf<kotlin.Byte> */> /* = kotlinx.cinterop.CPointerVarOf<kotlinx.cinterop.CPointer<kotlinx.cinterop.ByteVarOf<kotlin.Byte>>> */>?
//) {
//    jvmapi.GetClassSignature(klass, signature_ptr, generic_ptr)
//}//
//@CName("GetMethodDeclaringClass")
//fun GetMethodDeclaringClass(
//    method: jvmapi.jmethodID? /* = kotlinx.cinterop.CPointer<cnames.structs._jmethodID>? */,
//    declaring_class_ptr: kotlinx.cinterop.CValuesRef<jvmapi.jclassVar /* = kotlinx.cinterop.CPointerVarOf<jvmapi.jclass /* = kotlinx.cinterop.CPointer<cnames.structs._jobject> */> */>?
//) {
//    jvmapi.GetMethodDeclaringClass(method, declaring_class_ptr)
//}

//@CName("GetStackTrace")
//fun GetStackTrace(
//    thread: jvmapi.jthread? /* = kotlinx.cinterop.CPointer<cnames.structs._jobject>? */,
//    start_depth: jvmapi.jint /* = kotlin.Int */,
//    max_frame_count: jvmapi.jint /* = kotlin.Int */,
//    frame_buffer: kotlinx.cinterop.CValuesRef<jvmapi.jvmtiFrameInfo /* = jvmapi._jvmtiFrameInfo */>?,
//    count_ptr: kotlinx.cinterop.CValuesRef<jvmapi.jintVar /* = kotlinx.cinterop.IntVarOf<jvmapi.jint /* = kotlin.Int */> */>?
//) {
//    jvmapi.GetStackTrace(thread, start_depth, max_frame_count, frame_buffer, count_ptr)
//}

//@CName("GetLocalVariableTable")
//fun GetLocalVariableTable(
//    method: jvmapi.jmethodID? /* = kotlinx.cinterop.CPointer<cnames.structs._jmethodID>? */,
//    entry_count_ptr: kotlinx.cinterop.CValuesRef<jvmapi.jintVar /* = kotlinx.cinterop.IntVarOf<jvmapi.jint /* = kotlin.Int */> */>?,
//    table_ptr: kotlinx.cinterop.CValuesRef<kotlinx.cinterop.CPointerVar<jvmapi.jvmtiLocalVariableEntry /* = jvmapi._jvmtiLocalVariableEntry */> /* = kotlinx.cinterop.CPointerVarOf<kotlinx.cinterop.CPointer<jvmapi.jvmtiLocalVariableEntry /* = jvmapi._jvmtiLocalVariableEntry */>> */>?
//) {
//    jvmapi.GetLocalVariableTable(method, entry_count_ptr, table_ptr)
//}


@CName("sendToSocket")
fun sendToSocket(pluginId: CPointer<ByteVar>, message: CPointer<ByteVar>) {
    MessageQueue.sendMessage(pluginId.toKString(), message.toKString())
}


@CName("currentThread")
fun currentThread() = memScoped {
    val threadAllocation = alloc<jthreadVar>()
    GetCurrentThread(threadAllocation.ptr)
    threadAllocation.value
}

@CName("drillRequest")
fun drillRequest() = drillCRequest()?.get()


fun drillCRequest(thread: jthread? = currentThread()) = memScoped {
    val drillReq = alloc<COpaquePointerVar>()
    GetThreadLocalStorage(thread, drillReq.ptr)
    drillReq.value?.asStableRef<DrillRequest>()
}


@CName("jvmtix")
fun jvmti(): CPointer<jvmtiEnvVar>? {
    return gdata?.pointed?.jvmti
}

@CName("jvmtiCallbacks")
fun jvmtiCallback(): jvmtiEventCallbacks? {
    return gjavaVMGlob?.pointed?.callbackss
}

@CName("SetEventCallbacksP")
fun jvmti(
    callbacks: kotlinx.cinterop.CValuesRef<jvmapi.jvmtiEventCallbacks>?,
    size_of_callbacks: jvmapi.jint /* = kotlin.Int */
) {
    SetEventCallbacks(callbacks, size_of_callbacks)
}

@CName("enableJvmtiEventBreakpoint")
fun enableJvmtiEventBreakpoint(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_BREAKPOINT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_BREAKPOINT = true
}

@CName("enableJvmtiEventClassFileLoadHook")
fun enableJvmtiEventClassFileLoadHook(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_CLASS_FILE_LOAD_HOOK = true
}

@CName("enableJvmtiEventClassLoad")
fun enableJvmtiEventClassLoad(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_LOAD, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_CLASS_LOAD = true
}

@CName("enableJvmtiEventClassPrepare")
fun enableJvmtiEventClassPrepare(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_PREPARE, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_CLASS_PREPARE = true
}

@CName("enableJvmtiEventCompiledMethodLoad")
fun enableJvmtiEventCompiledMethodLoad(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_COMPILED_METHOD_LOAD, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_COMPILED_METHOD_LOAD = true
}

@CName("enableJvmtiEventCompiledMethodUnload")
fun enableJvmtiEventCompiledMethodUnload(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_COMPILED_METHOD_UNLOAD, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_COMPILED_METHOD_UNLOAD = true
}

@CName("enableJvmtiEventDataDumpRequest")
fun enableJvmtiEventDataDumpRequest(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_DATA_DUMP_REQUEST, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_DATA_DUMP_REQUEST = true
}

@CName("enableJvmtiEventDynamicCodeGenerated")
fun enableJvmtiEventDynamicCodeGenerated(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_DYNAMIC_CODE_GENERATED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_DYNAMIC_CODE_GENERATED = true
}

@CName("enableJvmtiEventException")
fun enableJvmtiEventException(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_EXCEPTION = true
}

@CName("enableJvmtiEventExceptionCatch")
fun enableJvmtiEventExceptionCatch(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_EXCEPTION_CATCH, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_EXCEPTION_CATCH = true
}

@CName("enableJvmtiEventFieldAccess")
fun enableJvmtiEventFieldAccess(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_FIELD_ACCESS, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_FIELD_ACCESS = true
}

@CName("enableJvmtiEventFieldModification")
fun enableJvmtiEventFieldModification(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_FIELD_MODIFICATION, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_FIELD_MODIFICATION = true
}

@CName("enableJvmtiEventFramePop")
fun enableJvmtiEventFramePop(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_FRAME_POP, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_FRAME_POP = true
}

@CName("enableJvmtiEventGarbageCollectionFinish")
fun enableJvmtiEventGarbageCollectionFinish(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_GARBAGE_COLLECTION_FINISH, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_GARBAGE_COLLECTION_FINISH = true
}

@CName("enableJvmtiEventGarbageCollectionStart")
fun enableJvmtiEventGarbageCollectionStart(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_GARBAGE_COLLECTION_START, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_GARBAGE_COLLECTION_START = true
}

@CName("enableJvmtiEventMethodEntry")
fun enableJvmtiEventMethodEntry(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_METHOD_ENTRY = true
}

@CName("enableJvmtiEventMethodExit")
fun enableJvmtiEventMethodExit(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_METHOD_EXIT = true
}

@CName("enableJvmtiEventMonitorContendedEnter")
fun enableJvmtiEventMonitorContendedEnter(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_MONITOR_CONTENDED_ENTER, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_CONTENDED_ENTER = true
}

@CName("enableJvmtiEventMonitorContendedEntered")
fun enableJvmtiEventMonitorContendedEntered(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_MONITOR_CONTENDED_ENTERED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_CONTENDED_ENTERED = true
}

@CName("enableJvmtiEventMonitorWait")
fun enableJvmtiEventMonitorWait(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_MONITOR_WAIT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_WAIT = true
}

@CName("enableJvmtiEventMonitorWaited")
fun enableJvmtiEventMonitorWaited(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_MONITOR_WAITED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_WAITED = true
}

@CName("enableJvmtiEventNativeMethodBind")
fun enableJvmtiEventNativeMethodBind(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_NATIVE_METHOD_BIND, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_NATIVE_METHOD_BIND = true
}

@CName("enableJvmtiEventObjectFree")
fun enableJvmtiEventObjectFree(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_OBJECT_FREE, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_OBJECT_FREE = true
}

@CName("enableJvmtiEventResourceExhausted")
fun enableJvmtiEventResourceExhausted(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_RESOURCE_EXHAUSTED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_RESOURCE_EXHAUSTED = true
}

@CName("enableJvmtiEventSingleStep")
fun enableJvmtiEventSingleStep(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_SINGLE_STEP, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_SINGLE_STEP = true
}

@CName("enableJvmtiEventThreadEnd")
fun enableJvmtiEventThreadEnd(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_THREAD_END, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_THREAD_END = true
}

@CName("enableJvmtiEventThreadStart")
fun enableJvmtiEventThreadStart(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_THREAD_START, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_THREAD_START = true
}

@CName("enableJvmtiEventVmDeath")
fun enableJvmtiEventVmDeath(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_DEATH = true
}

@CName("enableJvmtiEventVmInit")
fun enableJvmtiEventVmInit(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_INIT = true
}

@CName("enableJvmtiEventVmObjectAlloc")
fun enableJvmtiEventVmObjectAlloc(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_OBJECT_ALLOC, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_OBJECT_ALLOC = true
}

@CName("enableJvmtiEventVmStart")
fun enableJvmtiEventVmStart(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_START, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_START = true
}

@CName("disableJvmtiEventBreakpoint")
fun disableJvmtiEventBreakpoint(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_BREAKPOINT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_BREAKPOINT = false
}

@CName("disableJvmtiEventClassFileLoadHook")
fun disableJvmtiEventClassFileLoadHook(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_CLASS_FILE_LOAD_HOOK = false
}

@CName("disableJvmtiEventClassLoad")
fun disableJvmtiEventClassLoad(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_LOAD, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_CLASS_LOAD = false
}

@CName("disableJvmtiEventClassPrepare")
fun disableJvmtiEventClassPrepare(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_CLASS_PREPARE, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_CLASS_PREPARE = false
}

@CName("disableJvmtiEventCompiledMethodLoad")
fun disableJvmtiEventCompiledMethodLoad(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_COMPILED_METHOD_LOAD, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_COMPILED_METHOD_LOAD = false
}

@CName("disableJvmtiEventCompiledMethodUnload")
fun disableJvmtiEventCompiledMethodUnload(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_COMPILED_METHOD_UNLOAD, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_COMPILED_METHOD_UNLOAD = false
}

@CName("disableJvmtiEventDataDumpRequest")
fun disableJvmtiEventDataDumpRequest(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_DATA_DUMP_REQUEST, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_DATA_DUMP_REQUEST = false
}

@CName("disableJvmtiEventDynamicCodeGenerated")
fun disableJvmtiEventDynamicCodeGenerated(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_DYNAMIC_CODE_GENERATED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_DYNAMIC_CODE_GENERATED = false
}

@CName("disableJvmtiEventException")
fun disableJvmtiEventException(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_EXCEPTION, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_EXCEPTION = false
}

@CName("disableJvmtiEventExceptionCatch")
fun disableJvmtiEventExceptionCatch(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_EXCEPTION_CATCH, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_EXCEPTION_CATCH = false
}

@CName("disableJvmtiEventFieldAccess")
fun disableJvmtiEventFieldAccess(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_FIELD_ACCESS, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_FIELD_ACCESS = false
}

@CName("disableJvmtiEventFieldModification")
fun disableJvmtiEventFieldModification(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_FIELD_MODIFICATION, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_FIELD_MODIFICATION = false
}

@CName("disableJvmtiEventFramePop")
fun disableJvmtiEventFramePop(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_FRAME_POP, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_FRAME_POP = false
}

@CName("disableJvmtiEventGarbageCollectionFinish")
fun disableJvmtiEventGarbageCollectionFinish(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_GARBAGE_COLLECTION_FINISH, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_GARBAGE_COLLECTION_FINISH = false
}

@CName("disableJvmtiEventGarbageCollectionStart")
fun disableJvmtiEventGarbageCollectionStart(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_GARBAGE_COLLECTION_START, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_GARBAGE_COLLECTION_START = false
}

@CName("disableJvmtiEventMethodEntry")
fun disableJvmtiEventMethodEntry(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_METHOD_ENTRY, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_METHOD_ENTRY = false
}

@CName("disableJvmtiEventMethodExit")
fun disableJvmtiEventMethodExit(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_METHOD_EXIT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_METHOD_EXIT = false
}

@CName("disableJvmtiEventMonitorContendedEnter")
fun disableJvmtiEventMonitorContendedEnter(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_MONITOR_CONTENDED_ENTER, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_CONTENDED_ENTER = false
}

@CName("disableJvmtiEventMonitorContendedEntered")
fun disableJvmtiEventMonitorContendedEntered(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_MONITOR_CONTENDED_ENTERED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_CONTENDED_ENTERED = false
}

@CName("disableJvmtiEventMonitorWait")
fun disableJvmtiEventMonitorWait(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_MONITOR_WAIT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_WAIT = false
}

@CName("disableJvmtiEventMonitorWaited")
fun disableJvmtiEventMonitorWaited(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_MONITOR_WAITED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_MONITOR_WAITED = false
}

@CName("disableJvmtiEventNativeMethodBind")
fun disableJvmtiEventNativeMethodBind(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_NATIVE_METHOD_BIND, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_NATIVE_METHOD_BIND = false
}

@CName("disableJvmtiEventObjectFree")
fun disableJvmtiEventObjectFree(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_OBJECT_FREE, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_OBJECT_FREE = false
}

@CName("disableJvmtiEventResourceExhausted")
fun disableJvmtiEventResourceExhausted(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_RESOURCE_EXHAUSTED, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_RESOURCE_EXHAUSTED = false
}

@CName("disableJvmtiEventSingleStep")
fun disableJvmtiEventSingleStep(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_SINGLE_STEP, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_SINGLE_STEP = false
}

@CName("disableJvmtiEventThreadEnd")
fun disableJvmtiEventThreadEnd(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_THREAD_END, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_THREAD_END = false
}

@CName("disableJvmtiEventThreadStart")
fun disableJvmtiEventThreadStart(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_THREAD_START, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_THREAD_START = false
}

@CName("disableJvmtiEventVmDeath")
fun disableJvmtiEventVmDeath(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_VM_DEATH, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_DEATH = false
}

@CName("disableJvmtiEventVmInit")
fun disableJvmtiEventVmInit(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_VM_INIT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_INIT = false
}

@CName("disableJvmtiEventVmObjectAlloc")
fun disableJvmtiEventVmObjectAlloc(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_VM_OBJECT_ALLOC, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_OBJECT_ALLOC = false
}

@CName("disableJvmtiEventVmStart")
fun disableJvmtiEventVmStart(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_VM_START, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_START = false
}

@CName("getPlugin")
fun getPlugin(id: CPointer<ByteVar>): NativePart<*>? {
    return PluginManager[id.toKString()]?.np
}


@Suppress("UNCHECKED_CAST")
@CName("addPluginToRegistry")
fun addPluginToRegistry(plugin: NativePart<*>) {
    println("[TEMP] Try to addNativePluginPart to registry")
    try {
        val agentPluginPart: AgentPart<Any, Any>? = PluginManager[plugin.id.toKString()] as AgentPart<Any, Any>?
        if (agentPluginPart != null) {
            agentPluginPart.np = plugin as NativePart<Any>
            println("[TEMP] native part added.")
        } else {
            println("[WARNING!!!!!!!] CANT FIND THE ${plugin.id.toKString()} plug in manager. ")
        }

    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
}