package com.arash.basemodule.tools.vmvglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.arash.basemodule.BaseModule;
import com.arash.basemodule.contracts.Consumer;
import com.arash.basemodule.contracts.Observable;
import com.arash.basemodule.contracts.Observer;
import com.arash.basemodule.tools.Utils;
import com.arash.basemodule.tools.sessionmanager.SessionRepository;
import com.arash.basemodule.tools.vmvglue.contracts.Any;
import com.arash.basemodule.tools.vmvglue.contracts.ViewListenerProvider;
import com.arash.basemodule.tools.vmvglue.contracts.VmBindInfo;
import com.arash.basemodule.tools.vmvglue.contracts.XmlBindInfo;

import org.codejargon.feather.Key;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * !This class depends on Feather library for dependency injection. Edit this file to hire another DI lib<br/><br/>
 * This class does two jobs in MVVM pattern:<br/>
 * 1. It binds declared views in activity, fragment, ... to their xml id if there is any @XmlBindInfo annotation (initialization)<br/>
 * 2. It binds declared views in activity, fragment, ... to view-model if there is any @VmBindInfo. (one-way or two-way. It's up to you)<br/>
 * By event in view object, I will call view-model setter and also I can register for changes in view-model, then I will call setter on view object
 */
public class BindProcessor {
    /**
     * Binding data holder
     */
    private static class BindData {
        Object viewModelObj;// ViewModel object
        List<FieldData> lst = new ArrayList<>();// list of fields for binding xml
        Map<Class<?>, Method[]> tempMethodMemo;
        boolean inUse;

        Method[] getMethodsOf(Class<?> cls) {
            if (tempMethodMemo == null)
                tempMethodMemo = new HashMap<>();
            Method[] methods = tempMethodMemo.get(cls);
            if (methods == null) {
                methods = cls.getMethods();
                tempMethodMemo.put(cls, methods);
            }
            return methods;
        }

        void clearTempMemo() {
            tempMethodMemo = null;
        }
    }

    private static class FieldData {
        Field field;// the element field of View
        Object elm;// the element instance of View
        boolean bindToXml;// this element needs to be bind to a resource Id
        MethodData md;// information about all getters && setters

        FieldData(Field field) {
            this.field = field;
        }
    }

    /**
     * Required information for calling methods of View & ViewModel
     */
    private static class MethodData {
        Method vSetterMethod;// view-setter-method
        Class<?> vSetterParam;// view-setter-method parameter. This one helps to pick the correct method in case of polymorphism
        Method vGetter;// view-getter-method
        String vEventListenerRegisterer;// this one, helps to catch an instance via Feather

        Method vmSetterMethod;// view-model-setter-method
        Class<?> vmSetterParam;// view-model-setter-method parameter. This one helps to pick the correct method in case of polymorphism
        Method vmGetterMethod;// view-model-getter-method
        boolean registerForVmChanges;// should view listens for view-model changes

        Observer<Object> vmObserver;// view-model observer. This instance observes changes of view-model variable. We need to store the instance to be able to unregister it
        Consumer<Object> vEventConsumer;// To unregister view event consumer. It may leads to memory leak if we take no action on dismiss of the view

        Observable<Object> vmGetterOutput;// it is view-model getter output to make the whole process a little bit faster
        ViewListenerProvider viewListenerProvider;// it provides register/unregister methods for the view element event

        void setVMethods(Method setter, Class<?> setterParam, Method getter, String eventListenerRegisterer) {
            vSetterMethod = setter;
            vSetterParam = setterParam;
            vGetter = getter;
            vEventListenerRegisterer = eventListenerRegisterer;
        }

        void setMvMethods(Method setter, Class<?> setterParam, Method getter, boolean enableRegistering) throws Exception {
            vmSetterMethod = setter;
            vmSetterParam = setterParam;
            vmGetterMethod = getter;
            registerForVmChanges = enableRegistering;
            if (registerForVmChanges && !Observable.class.isAssignableFrom(vmGetterMethod.getReturnType()))
                throw new Exception("as registerForVmChanges() is set to true, " + vmGetterMethod.getName() + " must return " + Observable.class.getName() + " while it is not");
        }
    }

    /**
     * @see #init(Object, int, Object, boolean)
     */
    public static void init(Object viewObj) {
        init(viewObj, 0, null);
    }

    /**
     * @see #init(Object, int, Object, boolean)
     */
    public static void init(Object viewObj, Object viewModel) {
        init(viewObj, 0, viewModel);
    }

    /**
     * @see #init(Object, int, Object, boolean)
     */
    public static void init(Object viewObj, int nodeLevel) {
        init(viewObj, nodeLevel, null);
    }

    /**
     * @see #init(Object, int, Object, boolean)
     */
    public static void init(Object viewObj, int nodeLevel, Object viewModelObj) {
        init(viewObj, nodeLevel, viewModelObj, false);
    }

    /**
     * Every thing starts from here.<br/>
     * For example, you are writing an Activity. Normally, you overload onCreate() method and find views, initialize theme, ... .<br/>
     * Instead all of theme, call this method and pass your Activity instance. It also could be Fragment, FragmentActivity or Dialog.<br/>
     * If your Activity extends another one and there are some fields needed to be managed too, pass in 1 as nodeIndex.
     * nodeIndex can be any other positive number including 0.
     * I will search all parents till I get to the Object class. So do not worry, I handle parent fields too.<br/>
     * On configuration change event, I will restore previous view-model. This is done by SessionRepository class, therefore created sessions will remain till you call finish on this class.
     * Otherwise, whenever you get back to the mentioned activity, old data will be loaded
     *
     * @param viewObj      your target class instance (Activity, Fragment, FragmentActivity, Dialog)
     * @param nodeLevel    for current class is 0. If you are willing to get parents scanned too, pass in 1(parent), 2(grandparent), ...
     * @param viewModelObj target view-model object
     * @param justBind     by default loadDataFromViewModelIntoView(), registerForViewModelChanges() and registerForViewChanges() get called after binding to xml. You can disable that by passing in true
     */
    public static void init(Object viewObj, int nodeLevel, Object viewModelObj, boolean justBind) {
        try {
            BindData bindData = getBindData(viewObj, nodeLevel, viewModelObj);
            if (!justBind && bindData.viewModelObj != null) {
                loadDataFromViewModelIntoView(bindData);
                registerForViewModelChanges(bindData);
                registerForViewChanges(bindData);
            }
        } catch (Exception e) {
            throw new RuntimeException("BindProcessor failed", e);
        }
    }

    /**
     * restore or create new BindData object
     *
     * @param viewObj   view object
     * @param nodeLevel hierarchy level
     * @param viewModel ViewModel object
     * @return an instance of BindData
     * @throws Exception In case of finding ViewModel or any where during reflection operations
     */
    private static BindData getBindData(Object viewObj, int nodeLevel, Object viewModel) throws Exception {
        SessionRepository.Session session = getSession(viewObj);
        BindData bindData = getBindDataObject(session);
        if (bindData == null) {// if no BindData exists, create one and init it
            bindData = new BindData();
            findAnnotatedFields(viewObj, nodeLevel, bindData);// extract all field which are annotated by @XmlBindInfo or @VmBindInfo
            bindElementsToXml(viewObj, bindData);
            if (viewModel != null) {
                bindData.viewModelObj = viewModel;
                extractMethods(bindData);// put setters/getters of view & view-model objects into BindData
            }
            session.put("bind_data", bindData);// put data into the session for later use
        } else {// a BindData object is restored, so there is no need to init that, but binding to xml is necessary
            bindElementsToXml(viewObj, bindData);
        }
        return bindData;
    }

    /**
     * @param session a session related to this class which is given by getSession() method
     * @return an BindData instance, a brand new one or from repository
     */
    private static BindData getBindDataObject(SessionRepository.Session session) {
        return (BindData) session.get("bind_data", null);
    }

    /**
     * Trace back the whole hierarchy till Object class to find all annotated fields (scope doesn't matter)
     *
     * @param viewObj   The view instance, Activity, Fragment, ...
     * @param nodeLevel down to 0(hierarchy level)
     * @param bindData  An instance of BindData (new one or restored from SessionRepository)
     */
    private static void findAnnotatedFields(Object viewObj, int nodeLevel, BindData bindData) {
        Class<?> cls = viewObj.getClass();
        appendFields(bindData, cls.getFields());
        for (int i = 0; i <= nodeLevel && cls != Object.class; i++) {
            appendFields(bindData, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }
    }

    /**
     * Append annotated fields of a hierarchy class
     *
     * @param bindData BindData instance
     * @param fields   all the fields including private, protected, public and packaged scope
     * @see #findAnnotatedFields(Object, int, BindData)
     */
    private static void appendFields(BindData bindData, Field[] fields) {
        boolean xmlBind, vmBind;
        for (Field f : fields) {
            xmlBind = f.isAnnotationPresent(XmlBindInfo.class);
            vmBind = f.isAnnotationPresent(VmBindInfo.class);
            if (xmlBind || vmBind) {
                f.setAccessible(true);
                FieldData fd = new FieldData(f);
                fd.bindToXml = xmlBind;
                bindData.lst.add(fd);
                if (vmBind)
                    fd.md = new MethodData();
            }
        }
    }

    /**
     * If the element of view has come from xml (view or string resource), This method binds it their relative resources.
     * If the element is of type View class, root node of view is necessary, then calling rootView.findViewById(resId) would be enough
     * to catch an instance.<br/>
     * Strings would be fetched from string resource
     *
     * @param viewObj  view instance
     * @param bindData BindData instance
     * @throws Exception any exception related to reflection during binding
     */
    private static void bindElementsToXml(Object viewObj, BindData bindData) throws Exception {
        View rootView = null;
        for (FieldData fd : bindData.lst) {
            if (fd.bindToXml) {
                XmlBindInfo val = fd.field.getAnnotation(XmlBindInfo.class);
                assert val != null;
                Class<?> cls = fd.field.getType();
                if (View.class.isAssignableFrom(cls)) {// target field is view
                    if (rootView == null)
                        rootView = getRootViewFromContainerInstance(viewObj);
                    fd.elm = findView(rootView, val.value());
                    if (fd.elm == null)
                        throw new Exception(String.format("view %s.%s not found by given id", viewObj.getClass().getName(), fd.field.getName()));
                    fd.field.set(viewObj, fd.elm);
                } else if (cls == String.class) { // target field is string
                    fd.elm = Utils.getString(val.value());
                    if (fd.elm == null)
                        throw new Exception(String.format("String %s.%s not found by given id", viewObj.getClass().getName(), fd.field.getName()));
                    fd.field.set(viewObj, fd.elm);
                } else {
                    throw new Exception(String.format("binding for field %s.%s failed. Only views and strings are allowed", viewObj.getClass().getName(), fd.field.getName()));
                }
            }
        }
    }

    /**
     * @param rootView view instance in MVVM model
     * @param initInfo R.id
     * @return view instance from xml
     * @see #bindElementsToXml(Object, BindData)
     */
    private static View findView(View rootView, int initInfo) {
        return rootView.findViewById(initInfo);
    }

    /**
     * I will check out view instance, if it is Activity, Fragment, Dialog or android.app.Fragment I will return back the root view
     *
     * @param viewObj view instance in MVVM
     * @return view instance
     * @throws Exception if I can find no root view
     * @see #bindElementsToXml(Object, BindData)
     */
    @SuppressLint({"NewApi"})
    private static View getRootViewFromContainerInstance(Object viewObj) throws Exception {
        if (viewObj instanceof Activity) {
            return ((Activity) viewObj).getWindow().getDecorView().getRootView();
        }
        if (viewObj instanceof Fragment) {
            return ((Fragment) viewObj).getView();
        }
        if (viewObj instanceof Dialog) {
            return ((Dialog) viewObj).getWindow().getDecorView();
        }
        if (viewObj instanceof android.app.Fragment) {
            return ((android.app.Fragment) viewObj).getView();
        }
        throw new Exception("sorry I could not fetch the root view. Because your target object is not any kind of Activity, Fragment or Dialog");
    }

    /**
     * To do set/get operation, I need to fetch the relative methods via reflection. This method is responsible for extracting desired methods from view and view-model objects
     *
     * @param bindData BindData instance. This object contains view-model object, so there is no need to pass it separately
     * @throws Exception any reflection related exception
     */
    private static void extractMethods(BindData bindData) throws Exception {
        /*
         * BindProcessor is due to connect view to view-model and vice versa via reflection:
         * to get notified when the variable in view-model changes, view-model must return an observable via its getter
         * checkout the following schema:
         *  ______________     ______________     _____________________
         * |        event:|-->|              |-->|:setter              |
         * | view  getter:|-->|   BindProc   |   |          view-model |
         * |       setter:|<--| (reflection) |<--|:getter:observable   |
         * |______________|   |______________|   |_____________________|
         */
        for (FieldData fd : bindData.lst) {
            // catch annotations
            if (fd.md != null) {
                VmBindInfo modelInfo = fd.field.getAnnotation(VmBindInfo.class);

                // catch desired methods of view and view-model
                extractMethods(bindData, modelInfo, fd, bindData.viewModelObj);
            }
        }
        bindData.clearTempMemo();
    }

    /**
     * Extract method for every FieldData
     *
     * @param bindInfo     annotation information
     * @param fd           field data
     * @param viewModelObj view-model instance
     */
    private static void extractMethods(BindData bindData, VmBindInfo bindInfo, FieldData fd, Object viewModelObj) throws Exception {
        // if an event is defined for view then a setter must be declared for view-model
        if ((bindInfo.elmEventRegistererName().length() > 0 || bindInfo.elmGetter().length() > 0) && bindInfo.vmSetter().length() == 0) {
            throw new Exception("as elmEventRegistererName() is set, I need to call vmSetter() when an event happens, but it is not defined");
        }

        // if a getter is defined for view-model then a setter must be declared for view
        if (bindInfo.vmGetter().length() > 0 && bindInfo.elmSetter().length() == 0) {
            throw new Exception("as vmGetter() is set, I need to call elmSetter() to load data into view, but it is not defined");
        }

        // if view must lister to view-model changes then vmGetter() must be set and its return type must be Observable. Type check happens at setMvMethods() method
        if (bindInfo.registerForVmChanges() && bindInfo.vmGetter().length() == 0) {
            throw new Exception("as registerForVmChanges() is set to true, vmGetter() must be defined too while it is not");
        }

        // to prevent creating Class[] two times (check out a little bit further), I create one with length of 2
        Class<?>[] params = new Class<?>[]{bindInfo.elmSetterParam(), Void.class};
        Method[] methods = findMethods(bindData, fd.elm, new String[]{bindInfo.elmSetter(), bindInfo.elmGetter()}, params);
        fd.md.setVMethods(methods[0], params[0], methods[1], bindInfo.elmEventRegistererName());

        // instead of creating another Class[], use the previous one
        params[0] = bindInfo.vmSetterParam();
        methods = findMethods(bindData, viewModelObj, new String[]{bindInfo.vmSetter(), bindInfo.vmGetter()}, params);
        fd.md.setMvMethods(methods[0], params[0], methods[1], bindInfo.registerForVmChanges());
    }

    /**
     * As the array of all public methods is usually big, I try to fetch all desired methods at once
     *
     * @param targetObj   it would view  or view-model instances
     * @param methodNames array of desired methods
     * @param paramTypes  array of params of method (!each method must not have more than one parameter)
     * @return array of methods based on name argument with the same order
     * @throws Exception any reflection kind exception
     */
    private static Method[] findMethods(BindData bindData, Object targetObj, String[] methodNames, Class<?>[] paramTypes) throws Exception {
        Class<?> cls = targetObj.getClass();
        Method[] methods = bindData.getMethodsOf(cls);
        Method[] result = new Method[methodNames.length];
        for (int i = 0; i < result.length; i++) {
            if (methodNames[i].length() > 0) {
                result[i] = getMethod(cls, methods, methodNames[i], paramTypes[i]);
            }
        }
        return result;
    }

    /**
     * Loop through given method name with its parameter type
     *
     * @param containerCls container object type
     * @param methods      method array
     * @param name         target method name
     * @param paramType    parameters
     * @return the found method
     * @throws Exception any reflection kind exception
     */
    private static Method getMethod(Class<?> containerCls, Method[] methods, String name, Class<?> paramType) throws Exception {
        Method targetMethod = null;
        List<Method> foundMethods = new LinkedList<>();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                if (paramType == Any.class) {
                    targetMethod = m;
                    break;
                } else {
                    Class<?>[] p = m.getParameterTypes();
                    if (paramType == Void.class && p.length == 0) {
                        targetMethod = m;
                        break;
                    } else if (p.length == 1 && p[0].isAssignableFrom(paramType)) {
                        targetMethod = m;
                        break;
                    }
                }
                foundMethods.add(m);
            }
        }
        if (targetMethod == null) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("Found Method list in ").append(containerCls.getName()).append(": [\n");
            for (Method m : foundMethods)
                sb.append(m.toString()).append('\n');
            sb.append("]. None of theme matched\n")
                    .append(name).append('(').append(paramType.getName()).append(')');
            throw new Exception(sb.toString());
        }
        return targetMethod;
    }

    /**
     * If init() is called already, you can load view-model data into view by calling this method
     *
     * @param viewObj view instance in MVVM
     */
    public static void loadDataFromViewModelIntoView(Object viewObj) throws InvocationTargetException, IllegalAccessException {
        loadDataFromViewModelIntoView(getBindDataObject(getSession(viewObj)));
    }

    /**
     * @see #loadDataFromViewIntoViewModel(Object)
     */
    private static void loadDataFromViewModelIntoView(BindData bindData) throws IllegalAccessException, InvocationTargetException {
        if (bindData.viewModelObj != null) {
            for (FieldData fd : bindData.lst) {
                if (fd.md != null && fd.md.vmGetterMethod != null) {
                    Object result = fd.md.vmGetterMethod.invoke(bindData.viewModelObj);
                    if (result instanceof Observable)
                        result = ((Observable<Object>) result).getValue();
                    fd.md.vSetterMethod.invoke(fd.elm, result);
                }
            }
        }
    }

    /**
     * If you need to read all view values, this method is the easy way. init() must got call already
     *
     * @param viewObj view instance in MVVM
     */
    public static void loadDataFromViewIntoViewModel(Object viewObj) throws InvocationTargetException, IllegalAccessException {
        loadDataFromViewIntoViewModel(getBindDataObject(getSession(viewObj)), viewObj);
    }

    /**
     * @see #loadDataFromViewIntoViewModel(Object)
     */
    private static void loadDataFromViewIntoViewModel(BindData bindData, Object viewObj) throws IllegalAccessException, InvocationTargetException {
        for (FieldData fd : bindData.lst) {
            if (fd.md != null && fd.md.vGetter != null) {
                Object result = fd.md.vGetter.invoke(viewObj);
                fd.md.vmSetterMethod.invoke(bindData.viewModelObj, result);
            }
        }
    }

    /**
     * Update view when view-model changes through provided Observables at view-model
     *
     * @param viewObj view instance in MVVM
     */
    public static void registerForViewModelChanges(Object viewObj) throws InvocationTargetException, IllegalAccessException {
        registerForViewModelChanges(getBindDataObject(getSession(viewObj)));
    }

    /**
     * @see #registerForViewModelChanges(Object)
     */
    private static void registerForViewModelChanges(BindData bindData) throws InvocationTargetException, IllegalAccessException {
        if (bindData.viewModelObj != null) {
            for (FieldData fd : bindData.lst) {
                if (fd.md != null && fd.md.registerForVmChanges) {
                    fd.md.vmObserver = o -> {
                        try {
                            fd.md.vSetterMethod.invoke(fd.elm, o);
                        } catch (IllegalAccessException e) {
                            Utils.log(e);
                        } catch (InvocationTargetException e) {
                            Utils.log(e);
                        }
                    };
                    fd.md.vmGetterOutput = (Observable<Object>) fd.md.vmGetterMethod.invoke(bindData.viewModelObj);
                    fd.md.vmGetterOutput.observe(fd.md.vmObserver);
                }
            }
            bindData.inUse = true;
        }
    }

    /**
     * Notify view-model of changed of view via ViewListeners. Target ViewListener must be introduced to Feather class
     *
     * @param viewObj view instance in MVVM
     */
    public static void registerForViewChanges(Object viewObj) {
        registerForViewChanges(getBindDataObject(getSession(viewObj)));
    }

    /**
     * @see #registerForViewChanges(Object)
     */
    private static void registerForViewChanges(BindData bindData) {
        if (bindData.viewModelObj != null) {
            for (FieldData fd : bindData.lst) {
                if (fd.md != null && !fd.md.vEventListenerRegisterer.isEmpty()) {
                    fd.md.viewListenerProvider = BaseModule.feather.instance(Key.of(ViewListenerProvider.class, fd.md.vEventListenerRegisterer));
                    assert fd.md.viewListenerProvider != null;
                    fd.md.vEventConsumer = o -> {
                        try {
                            fd.md.vmSetterMethod.invoke(bindData.viewModelObj, o);
                        } catch (IllegalAccessException e) {
                            Utils.log(e);
                        } catch (InvocationTargetException e) {
                            Utils.log(e);
                        }
                    };
                    fd.md.viewListenerProvider.registerListener((View) fd.elm, fd.md.vEventConsumer);
                }
            }
            bindData.inUse = true;
        }
    }

    /**
     * Catch session by view instance
     */
    private static SessionRepository.Session getSession(Object viewObj) {
        return SessionRepository.getSession(viewObj.getClass().getName());
    }

    /**
     * If you have register observers for view-model, you can unregister them all on configuration change event
     *
     * @param viewObj target class
     */
    public static void unregisterObservers(Object viewObj) {
        SessionRepository.Session session = getSession(viewObj);
        BindData bindData = getBindDataObject(session);
        if (bindData != null) {
            for (FieldData fd : bindData.lst) {
                if (fd.md != null) {
                    if (fd.md.vmGetterOutput != null) {
                        fd.md.vmGetterOutput.removeObserver(fd.md.vmObserver);
                        fd.md.vmObserver = null;
                    }
                    if (fd.md.viewListenerProvider != null) {
                        fd.md.viewListenerProvider.unregisterListener((View) fd.elm, fd.md.vEventConsumer);
                        fd.md.vEventConsumer = null;
                    }
                }
            }
            bindData.inUse = false;
        }
    }

    /**
     * @see #finish(Object, boolean)
     */
    public static void finish(Object viewObj) {
        finish(viewObj, false);
    }

    /**
     * clear session and unregister observers if not needed any more
     *
     * @param viewObj     target object
     * @param clearMemory pass in true to unregister observers
     * @see #unregisterObservers(Object)
     */
    public static void finish(Object viewObj, boolean clearMemory) {
        SessionRepository.Session session = getSession(viewObj);
        BindData bd = getBindDataObject(session);
        if (bd != null && bd.inUse)
            unregisterObservers(viewObj);
        if (clearMemory) {
            SessionRepository.removeSession(session);
        }
    }

    /**
     * you can access your stored view-model object by this method
     *
     * @param viewObj current view object
     * @return stored view-model object or null
     */
    public static Object getViewModel(Object viewObj) {
        BindData bd = getBindDataObject(getSession(viewObj));
        return bd != null ? bd.viewModelObj : null;
    }
}
