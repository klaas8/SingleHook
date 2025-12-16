package org.lsposed.hijack.hook.util;

import android.text.TextUtils;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lsposed.hijack.hook.HookUtils;

public class CacheUtil extends HookUtils {

    public static class ClassData {
        public int MethodCount, FieldCount, InterfaceCount, Modifiers;
        public String ClassName, SuperClassName, SourceFile;
        public List<String> InterfaceNames;
        public List<ChildMethodData> Methods;
        public List<UsingFieldsData> Fields;
        public List<mAnnotations> Annotations;
        
        public boolean isOk() {
            try {
                return XposedHelpers.findClass(this.ClassName, mClassLoader) != null;
            } catch (Throwable err) {}
            return false;
        }
        
        public findChildMethod findMethod() {
            return new findChildMethod(this.Methods);
        }
        
        public findUsingFields findFields() {
            return new findUsingFields(this.Fields);
        }
        
        public void p() {
            String str = this.toString();
            try {
            	JSONObject Json = new JSONObject(str);
                l("⁩\n>>>\n" + Json.toString(4) + "\n<<<");
            } catch(Throwable err) {
                l(str);
            }
        }
        
        @Override
        public String toString() {
            JSONObject Json = new JSONObject();
            try {
                Json.put("className", this.ClassName);
                Json.put("superClassName", this.SuperClassName);
                Json.put("sourceFile", this.SourceFile);
                Json.put("modifiers", this.Modifiers);
                Json.put("methodCount", this.MethodCount);
                Json.put("fieldCount", this.FieldCount);
                Json.put("interfaceCount", this.InterfaceCount);
                Json.put("interfaceNames", new JSONArray(this.InterfaceNames));
                JSONArray newMethods = new JSONArray();
                Methods.forEach(v->{
                    try {
                        newMethods.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("methods", newMethods);
                JSONArray newFields = new JSONArray();
                Fields.forEach(v->{
                    try {
                    	newFields.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("fields", newFields);
                JSONArray ann = new JSONArray();
                this.Annotations.forEach(v->{
                    try {
                    	ann.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("annotations", ann);
            } catch(Throwable err) {}
            return Json.toString();
        }
    }
    
    public static class mAnnotations {
        public String type;
        public HashMap<String, String> Elements;
        
        @Override
        public String toString() {
            JSONObject Json = new JSONObject();
            try {
                Json.put("type", this.type);
                JSONObject mElements = new JSONObject();
                this.Elements.forEach((k,v)->{
                    try {
                    	mElements.put(k,v);
                    } catch(Throwable err) {}
                });
                Json.put("Elements", mElements);
            } catch(Throwable err) {}
            return Json.toString();
        }
    }
    
    public static class ann {
        public List<String> type = new ArrayList<>();
        public Element element;
        public static ann create() {
        	return new ann();
        }
        public ann type(List<String> nType) {
            if (nType == null) return this;
        	this.type.addAll(nType);
            return this;
        }
        public ann type(String nType) {
        	this.type.add(nType);
            return this;
        }
        public ann addElement(Element element) {
            this.element = element;
        	return this;
        }
        public class Element {
            public List<String> names = new ArrayList<>();
            public List<String> values = new ArrayList<>();
            public void name(String name) {
                this.names.add(name);
            }
            public void value(String value) {
                this.values.add(value);
            }
        }
    }
    
    public static interface IChild {
    	void call(ChildMethodData childMethod);
    }
    public static class findChildMethod {
        private ann ann;
        private List<ChildMethodData> data;
        private String ClassName, MethodName, ReturnTypeName;
        private List<String> paramTypes = new ArrayList<>(), usingStrings = new ArrayList<>();
        
        private boolean isStaticInitializer,
            isConstructor,
            isMethod,
            isNeedUsingStringEq,
            hasStaticInitializer,
            hasConstructor,
            hasMethod,
            hasModifiers,
            hasParamCount;
        private int Modifiers, paramCount;
        
        public findChildMethod(List<ChildMethodData> data) {
            this.data = data;
        }
        public findChildMethod addUsingStrings(String usingString) {
        	this.usingStrings.add(usingString);
            this.isNeedUsingStringEq = false;
            return this;
        }
        public findChildMethod usingStrings(String ...usingString) {
            Arrays.stream(usingString).forEach(using -> this.usingStrings.add(using));
            this.isNeedUsingStringEq = true;
            return this;
        }
        public findChildMethod usingStrings(List<String> usingString) {
            this.usingStrings = usingString;
            this.isNeedUsingStringEq = true;
            return this;
        }
        public findChildMethod paramTypes(String ...Types) {
            Arrays.stream(Types).forEach(type -> this.paramTypes.add(type));
            return this;
        }
        public findChildMethod paramTypes(List<String> Types) {
            this.paramTypes = Types;
            return this;
        }
        public findChildMethod paramCount(int count) {
            this.paramCount = count;
            this.hasParamCount = true;
            return this;
        }
        public findChildMethod className(String className) {
            this.ClassName = className;
            return this;
        }
        public findChildMethod name(String methodName) {
            this.MethodName = methodName;
            return this;
        }
        public findChildMethod returnType(String returnType) {
            this.ReturnTypeName = returnType;
            return this;
        }
        public findChildMethod modifiers(int Modifiers) {
            this.Modifiers = Modifiers;
            this.hasModifiers = true;
            return this;
        }
        public findChildMethod isConstructor(boolean isConstructor) {
            this.isConstructor = isConstructor;
            this.hasConstructor = true;
            return this;
        }
        public findChildMethod isStaticInitializer(boolean isStaticInitializer) {
            this.isStaticInitializer = isStaticInitializer;
            this.hasStaticInitializer = true;
            return this;
        }
        public findChildMethod isMethod(boolean isMethod) {
            this.isMethod = isMethod;
            this.hasMethod = true;
            return this;
        }
        public findChildMethod addAnnotation(ann ann) {
            this.ann = ann;
            return this;
        }
        public void hook(Object mCallback) {
        	fi(v->{
                v.hook(mCallback);
            });
        }
        public void fi(IChild iChild) {
        	if (data == null) return;
            data.forEach(method ->{
                if (!TextUtils.isEmpty(ClassName) && !method.ClassName.equals(ClassName)) return;
                if (!TextUtils.isEmpty(MethodName) && !method.MethodName.equals(MethodName)) return;
                if (!TextUtils.isEmpty(ReturnTypeName) && !method.ReturnTypeName.equals(ReturnTypeName)) return;
                if (paramTypes.size() > 0 && !String.join(",", paramTypes).equals(String.join(",", method.paramTypes))) return;
                if (usingStrings.size() > 0) {
                    if (isNeedUsingStringEq) {
                        if (!method.UsingStrings.stream().allMatch(using -> usingStrings.contains(using))) return;
                    } else {
                        if (!usingStrings.stream().allMatch(using -> method.UsingStrings.contains(using))) return;
                    }
                }
                if (hasStaticInitializer && isStaticInitializer != method.isStaticInitializer) return;
                if (hasConstructor && isConstructor != method.isConstructor) return;
                if (hasMethod && isMethod != method.isMethod) return;
                if (hasModifiers && Modifiers != method.Modifiers) return;
                if (hasParamCount && paramCount != method.paramTypes.size()) return;
                if (ann != null) {
                    List<String> mAnnType = new ArrayList<>();
                    List<String> mAnnName = new ArrayList<>();
                    List<String> mAnnValue = new ArrayList<>();
                    method.Annotations.forEach(v->{
                        mAnnType.add(v.type);
                        v.Elements.forEach((key,value) ->{
                            mAnnName.add(key);
                            mAnnValue.add(value);
                        });
                    });
                    if (!ann.type.stream().allMatch(v -> mAnnType.contains(v))) return;
                    if (!ann.element.names.stream().allMatch(v -> mAnnName.contains(v))) return;
                    if (!ann.element.values.stream().allMatch(v -> mAnnValue.contains(v))) return;
                }
                iChild.call(method);
            });
        }
    }
    
    public static interface IUsingFields {
    	void call(UsingFieldsData usingFields);
    }
    public static class findUsingFields {
        private ann ann;
        private List<UsingFieldsData> data;
        private String ClassName, FieldName, TypeName, UsingType;
        private boolean hasModifiers;
        private int Modifiers;
        
        public findUsingFields(List<UsingFieldsData> data) {
            this.data = data;
        }
        public findUsingFields className(String className) {
            this.ClassName = className;
            return this;
        }
        public findUsingFields fieldName(String fieldName) {
            this.FieldName = fieldName;
            return this;
        }
        public findUsingFields type(String type) {
            this.TypeName = type;
            return this;
        }
        public findUsingFields usingType(String usingType) {
            this.UsingType = usingType;
            return this;
        }
        public findUsingFields Modifiers(int Modifiers) {
            this.Modifiers = Modifiers;
            this.hasModifiers = true;
            return this;
        }
        public findUsingFields addAnnotation(ann ann) {
            this.ann = ann;
            return this;
        }
        public void fi(IUsingFields iUsingFields) {
        	if (data == null) return;
            data.forEach(field ->{
                if (!TextUtils.isEmpty(ClassName) && !field.ClassName.equals(ClassName)) return;
                if (!TextUtils.isEmpty(FieldName) && !field.FieldName.equals(FieldName)) return;
                if (!TextUtils.isEmpty(TypeName) && !field.TypeName.equals(TypeName)) return;
                if (!TextUtils.isEmpty(UsingType) && !field.UsingType.equalsIgnoreCase(UsingType)) return;
                if (hasModifiers && Modifiers != field.Modifiers) return;
                if (ann != null) {
                    List<String> mAnnType = new ArrayList<>();
                    List<String> mAnnName = new ArrayList<>();
                    List<String> mAnnValue = new ArrayList<>();
                    field.Annotations.forEach(v->{
                        mAnnType.add(v.type);
                        v.Elements.forEach((key,value) ->{
                            mAnnName.add(key);
                            mAnnValue.add(value);
                        });
                    });
                    if (!ann.type.stream().allMatch(v -> mAnnType.contains(v))) return;
                    if (!ann.element.names.stream().allMatch(v -> mAnnName.contains(v))) return;
                    if (!ann.element.values.stream().allMatch(v -> mAnnValue.contains(v))) return;
                }
                iUsingFields.call(field);
            });
        }
    }
    
    public static class MethodData {
        public String ClassName, MethodName, ReturnTypeName;
        public List<String> paramTypes, UsingStrings;
        public int Modifiers;
        public boolean isStaticInitializer, isConstructor, isMethod;
        public List<ChildMethodData> Callers;
        public List<ChildMethodData> Invokes;
        public List<UsingFieldsData> UsingFields;
        public List<mAnnotations> Annotations;
        
        public findChildMethod getInvokes() {
            return new findChildMethod(this.Invokes);
        }
        
        public findChildMethod getCallers() {
            return new findChildMethod(this.Callers);
        }
        
        public findUsingFields getUsingFields() {
            return new findUsingFields(this.UsingFields);
        }
        
        public void p() {
            String str = this.toString();
            try {
            	JSONObject Json = new JSONObject(str);
                l("⁩\n>>>\n" + Json.toString(4) + "\n<<<");
            } catch(Throwable err) {
                l(str);
            }
        }
        
        public boolean canHook() {
            return (!Modifier.isAbstract(this.Modifiers) && !Modifier.isInterface(this.Modifiers) && !Modifier.isNative(this.Modifiers));
        }
        
        public boolean isOk() {
            try {
                if (this.isConstructor) {
                    return XposedHelpers.findConstructorExactIfExists(this.ClassName, mClassLoader, this.paramTypes.toArray(new Object[0])) != null;
                } else if (this.isMethod){
                    return XposedHelpers.findMethodExactIfExists(this.ClassName, mClassLoader, this.MethodName, this.paramTypes.toArray(new Object[0])) != null;
                }
            } catch(Throwable err) {}
        	return false;
        }

        public Class<?> getClazz() {
            try {
                Class<?> clazz = XposedHelpers.findClass(this.ClassName, mClassLoader);
                if (clazz != null) return clazz;
            } catch (Throwable err) {
            }
            return null;
        }

        public Field[] getDeclaredFields() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getDeclaredFields();
                }
            } catch (Throwable err) {
            }
            return new Field[] {};
        }

        public Field[] getFields() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getFields();
                }
            } catch (Throwable err) {
            }
            return new Field[] {};
        }

        public Method[] getDeclaredMethods() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getDeclaredMethods();
                }
            } catch (Throwable err) {
            }
            return new Method[] {};
        }

        public Method[] getMethods() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getMethods();
                }
            } catch (Throwable err) {
            }
            return new Method[] {};
        }

        public Method getMethod() {
            try {
                return XposedHelpers.findMethodExact(this.ClassName, mClassLoader, this.MethodName, this.paramTypes.toArray(new Object[0]));
            } catch(Throwable e) {
                if (DEBUG) l(toString());
            }
            return null;
        }
        
        public void hook(Object param) {
            if (!canHook()) return;
            try {
                if (param == null) param = XC_MethodReplacement.returnConstant(null);
                XC_MethodHook mCallback = (param instanceof XC_MethodHook) ? (XC_MethodHook) param : XC_MethodReplacement.returnConstant(param);
                if (this.isConstructor) {
                    tryFindAndHookConstructor(this.MethodName, mClassLoader, this.paramTypes.toArray(new Object[0]), mCallback);
                } else if (this.isMethod){
                    tryHookMethod(getMethod(), mCallback);
                }
            } catch (Throwable e){}
        }
        
        @Override
        public String toString() {
            JSONObject Json = new JSONObject();
            try {
                Json.put("className", this.ClassName);
                Json.put("methodName", this.MethodName);
                Json.put("parameterTypes", new JSONArray(this.paramTypes));
                Json.put("usingStrings", new JSONArray(this.UsingStrings));
                Json.put("returnTypeName", this.ReturnTypeName);
                Json.put("modifiers", this.Modifiers);
                Json.put("isConstructor", this.isConstructor);
                Json.put("isMethod", this.isMethod);
                Json.put("isStaticInitializer", this.isStaticInitializer);
                JSONArray newCallers = new JSONArray();
                Callers.forEach(v->{
                    try {
                        newCallers.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("callers", newCallers);
                JSONArray newInvokes = new JSONArray();
                Invokes.forEach(v->{
                    try {
                        newInvokes.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("invokes", newInvokes);
                JSONArray newUsingFields = new JSONArray();
                UsingFields.forEach(v->{
                    try {
                    	newUsingFields.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("usingFields", newUsingFields);
                JSONArray ann = new JSONArray();
                this.Annotations.forEach(v->{
                    try {
                    	ann.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("annotations", ann);
            } catch(Throwable err) {}
            return Json.toString();
        }
    }

    public static class ChildMethodData {
        public String ClassName, MethodName, ReturnTypeName;
        public List<String> paramTypes, UsingStrings;
        public boolean isStaticInitializer, isConstructor, isMethod;
        public int Modifiers;
        public List<mAnnotations> Annotations;
        
        public void p() {
            String str = this.toString();
            try {
            	JSONObject Json = new JSONObject(str);
                l("⁩\n>>>\n" + Json.toString(4) + "\n<<<");
            } catch(Throwable err) {
                l(str);
            }
        }
        
        public boolean canHook() {
            return (!Modifier.isAbstract(this.Modifiers) && !Modifier.isInterface(this.Modifiers) && !Modifier.isNative(this.Modifiers));
        }
        
        public boolean isOk() {
            try {
                if (this.isConstructor) {
                    return XposedHelpers.findConstructorExactIfExists(this.ClassName, mClassLoader, this.paramTypes.toArray(new Object[0])) != null;
                } else if (this.isMethod){
                    return XposedHelpers.findMethodExactIfExists(this.ClassName, mClassLoader, this.MethodName, this.paramTypes.toArray(new Object[0])) != null;
                }
            } catch(Throwable err) {}
        	return false;
        }

        public Class<?> getClazz() {
            try {
                Class<?> clazz = XposedHelpers.findClass(this.ClassName, mClassLoader);
                if (clazz != null) return clazz;
            } catch (Throwable err) {
            }
            return null;
        }

        public Field[] getDeclaredFields() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getDeclaredFields();
                }
            } catch (Throwable err) {
            }
            return new Field[] {};
        }

        public Field[] getFields() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getFields();
                }
            } catch (Throwable err) {
            }
            return new Field[] {};
        }

        public Method[] getDeclaredMethods() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getDeclaredMethods();
                }
            } catch (Throwable err) {
            }
            return new Method[] {};
        }

        public Method[] getMethods() {
            try {
                Class<?> clazz = getClazz();
                if (clazz != null) {
                    return clazz.getMethods();
                }
            } catch (Throwable err) {
            }
            return new Method[] {};
        }

        public Method getMethod() {
            try {
                return XposedHelpers.findMethodExact(this.ClassName, mClassLoader, this.MethodName, this.paramTypes.toArray(new Object[0]));
            } catch(Throwable e) {
                if (DEBUG) l(toString());
            }
            return null;
        }
        
        public void hook(Object param) {
            if (!canHook()) return;
            try {
                if (param == null) param = XC_MethodReplacement.returnConstant(null);
                XC_MethodHook mCallback = (param instanceof XC_MethodHook) ? (XC_MethodHook) param : XC_MethodReplacement.returnConstant(param);
                if (this.isConstructor) {
                    tryFindAndHookConstructor(this.MethodName, mClassLoader, this.paramTypes.toArray(new Object[0]), mCallback);
                } else if (this.isMethod){
                    tryHookMethod(getMethod(), mCallback);
                }
            } catch (Throwable e){}
        }
        
        @Override
        public String toString() {
            JSONObject Json = new JSONObject();
            try {
                Json.put("className", this.ClassName);
                Json.put("methodName", this.MethodName);
                Json.put("parameterTypes", new JSONArray(this.paramTypes));
                Json.put("usingStrings", new JSONArray(this.UsingStrings));
                Json.put("returnTypeName", this.ReturnTypeName);
                Json.put("modifiers", this.Modifiers);
                Json.put("isConstructor", this.isConstructor);
                Json.put("isMethod", this.isMethod);
                Json.put("isStaticInitializer", this.isStaticInitializer);
                JSONArray ann = new JSONArray();
                this.Annotations.forEach(v->{
                    try {
                    	ann.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("annotations", ann);
            } catch(Throwable err) {}
            return Json.toString();
        }
    }

    public static class UsingFieldsData {
        public String ClassName, FieldName, TypeName, UsingType;
        public int Modifiers;
        public List<mAnnotations> Annotations;
        
        @Override
        public String toString() {
            JSONObject Json = new JSONObject();
            try {
                Json.put("className", this.ClassName);
                Json.put("fieldName", this.FieldName);
                Json.put("typeName", this.TypeName);
                Json.put("usingType", this.UsingType);
                Json.put("modifiers", this.Modifiers);
                JSONArray ann = new JSONArray();
                this.Annotations.forEach(v->{
                    try {
                    	ann.put(new JSONObject(v.toString()));
                    } catch(Throwable err) {}
                });
                Json.put("annotations", ann);
            } catch(Throwable err) {}
            return Json.toString();
        }
    }
}