# Mesh SDK
开发Mesh时，请先熟悉TuyaHomeSdk。Mesh的所有操作都建立在家庭数据已经初始化的基础上。
一个家庭里可以拥有多个mesh（建议一个家庭只创建一个），

# 1.Mesh
## 1.1 准备
权限增加

```
	<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    
    <uses-permission android:name="android.permission.BLUETOOTH" />
    
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    
```

## 1.2 Mesh 操作

### 创建Mesh

##### 【方法调用】
```
* @param meshName   mesh的名称（不超过16字节）
* @param callback   回调
void createBlueMesh(String meshName, ITuyaResultCallback<BlueMeshBean> callback);
```

##### 【代码范例】
``` java
TuyaHomeSdk.newHomeInstance("homeId").createBlueMesh("meshName", new ITuyaResultCallback<BlueMeshBean>() {
    @Override
    public void onError(String errorCode, String errorMsg) {
        Toast.makeText(mContext, "创建mesh失败  "+ errorMsg, Toast.LENGTH_LONG).show();
    }

    @Override	
    public void onSuccess(BlueMeshBean blueMeshBean) {
        Toast.makeText(mContext, "创建mesh成功", Toast.LENGTH_LONG).show();
    }
});
```

### 删除Mesh

##### 【代码范例】
```
TuyaHomeSdk.newBlueMeshDeviceInstance(meshId).removeMesh(new IResultCallback() {
    @Override
    public void onError(String errorCode, String errorMsg) {
	    Toast.makeText(mContext, "删除mesh失败  "+ errorMsg, Toast.LENGTH_LONG).show();
    }
	
    @Override
    public void onSuccess() {
	    Toast.makeText(mContext, "删除mesh成功", Toast.LENGTH_LONG).show();
    }
});

```

### 从缓存中获取Mesh数据
##### 【方法调用】
```
TuyaHomeSdk.newHomeInstance("homeId").getHomeBean().getMeshList()
```
##### 【代码范例】
```
ITuyaHome mTuyaHome = TuyaHomeSdk.newHomeInstance("homeId");
if (mTuyaHome.getHomeBean() != null){
	List<BlueMeshBean> meshList = mTuyaHome.getHomeBean().getMeshList();
	BlueMeshBean meshBean= meshList.get(0);
}            
```

### 从缓存中获取Mesh下的子设备数据
##### 【代码范例】
```
List<DeviceBean> meshSubDevList = TuyaHomeSdk.newBlueMeshDeviceInstance("meshId").getMeshSubDevList();
    
```


### Mesh初始化和销毁
建议在家庭切换的时候 销毁当前mesh  然后重新初始化家庭中的mesh

##### 【方法调用】
```
//初始化mesh
TuyaHomeSdk.getTuyaBlueMeshClient().initMesh(String meshId);       

//销毁当前mesh
TuyaHomeSdk.getTuyaBlueMeshClient().destroyMesh();       
```


### Mesh子设备连接和断开
##### 【描述】
ITuyaBlueMeshClient 提供 开始连接、断开连接、开启扫描、停止扫描

##### 【方法调用】
```
// 开启连接
TuyaHomeSdk.getTuyaBlueMeshClient().startClient(mBlueMeshBean);

//断开连接
TuyaHomeSdk.getTuyaBlueMeshClient().stopClient();

//开启扫描
TuyaHomeSdk.getTuyaBlueMeshClient().startSearch()

//停止扫描
TuyaHomeSdk.getTuyaBlueMeshClient().stopSearch();

```

##### 【注意事项】 
###### 开启连接后，会在后台不断的去扫描周围可连接设备，直到连接成功为止。
###### 后台一直扫描会消耗资源，可以通过通过开启扫描和停止扫描来控制后台的扫描
###### 当未startClient()时候，调用startSearch()和stopSearch()是没有效果的
###### 当已经连接到mesh网的时候，调用startSearch和stopSearch是没有效果的



## 1.3 Mesh 子设备入配网

### 扫描待配网子设备
##### 【描述】
扫描前需要检查蓝牙和位置权限

##### 【方法调用】
```
//开启扫描
mMeshSearch.startSearch();
//停止扫描
mMeshSearch.stopSearch();

```
##### 【代码范例】
```
ITuyaBlueMeshSearchListener iTuyaBlueMeshSearchListener=new ITuyaBlueMeshSearchListener() {
    @Override
    public void onSearched(SearchDeviceBean deviceBean) {

    }

    @Override
    public void onSearchFinish() {

    }
};

SearchBuilder searchBuilder = new SearchBuilder()
				.setMeshName("out_of_mesh")        //要扫描设备的名称（默认会是out_of_mesh，设备处于配网状态下的名称）
                .setTimeOut(100)        //扫描时长 单位秒
                .setTuyaBlueMeshSearchListener(iTuyaBlueMeshSearchListener).build();

ITuyaBlueMeshSearch mMeshSearch = TuyaHomeSdk.getTuyaBlueMeshConfig().newTuyaBlueMeshSearch(searchBuilder);

//开启扫描
mMeshSearch.startSearch();

//停止扫描
mMeshSearch.stopSearch();
```

### 子设备入网
##### 【描述】
子设备入网分为2种，一种是普通设备入网，一种是mesh网关入网

##### 【初始化参数配置】
```
//普通设备入网参数配置
TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new TuyaBlueMeshActivatorBuilder()
            .setSearchDeviceBeans(mSearchDeviceBeanList)
            //默认版本号
            .setVersion("1.0")
            .setBlueMeshBean(mMeshBean)
            //超时时间
            .setTimeOut(CONFIG_TIME_OUT)
            .setTuyaBlueMeshActivatorListener();
            
//mesh网关入网参数配置   
TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new TuyaBlueMeshActivatorBuilder()
            .setWifiSsid(mSsid)
            .setWifiPassword(mPassword)     
            .setSearchDeviceBeans(mSearchDeviceBeanList)
            //默认版本号
            .setVersion("2.2")
            .setBlueMeshBean(mMeshBean)
            .setHomeId("homeId")
            //超时时间
        	  .setTimeOut(CONFIG_TIME_OUT)
            .setTuyaBlueMeshActivatorListener();
```

##### 【参数说明】
###### 【入参】
```
* @param mSearchDeviceBeans     待配网的设备集合
* @param timeout    配网的超时时间设置，默认是100s.
* @param ssid       配网之后，设备工作WiFi的名称。（家庭网络）
* @param password   配网之后，设备工作WiFi的密码。（家庭网络)
* @param mMeshBean  MeshBean 
* @param homeId     设备要加入的Mesh网 所属家庭的HomeId
* @param version    普通设备配网是1.0    网关配网是2.2
```

###### 【出参】
ITuyaBlueMeshActivatorListener listener 配网回调接口

```
//单设备配网失败回调
void onError(String errorCode, String errorMsg);

@param errorCode:
13007       登录设备失败
13004       重置设备地址失败
13005       设备地址已满
13007       ssid为空
13011       配网超时

//单设备配网成功回调
void onSuccess(DeviceBean deviceBean);

//整个配网结束回调
void onFinish();

```

##### 【方法调用】

```
//开启配网
iTuyaBlueMeshActivator.startActivator();

//停止配网
iTuyaBlueMeshActivator.stopActivator();
```

##### 【代码范例】
```
//普通设备入网
TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new TuyaBlueMeshActivatorBuilder()
                .setSearchDeviceBeans(foundDevices)
                .setVersion("1.0")
                .setBlueMeshBean(mMeshBean)
                .setTimeOut(timeOut)
                .setTuyaBlueMeshActivatorListener(new ITuyaBlueMeshActivatorListener() {
                    @Override
                    public void onSuccess(DeviceBean deviceBean) {
                        L.d(TAG, "subDevBean onSuccess: " + deviceBean.getName());
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        L.d(TAG, "config mesh error" + errorCode + " " + errorMsg);
                    }

                    @Override
                    public void onFinish() {
                        L.d(TAG, "config mesh onFinish： ");
                    }
                });
ITuyaBlueMeshActivator iTuyaBlueMeshActivator = TuyaHomeSdk.getTuyaBlueMeshConfig().newActivator(tuyaBlueMeshActivatorBuilder);

iTuyaBlueMeshActivator.startActivator();


//网关入网
TuyaBlueMeshActivatorBuilder tuyaBlueMeshActivatorBuilder = new TuyaBlueMeshActivatorBuilder()
                .setWifiSsid(mSsid)
                .setWifiPassword(mPassword)
                .setSearchDeviceBeans(foundDevices)
                .setVersion("2.2")
                .setBlueMeshBean(mMeshBean)
                .setHomeId("homeId")
                .setTuyaBlueMeshActivatorListener(new ITuyaBlueMeshActivatorListener() {

                    @Override
                    public void onSuccess(DeviceBean devBean) {
                    	//单个设备配网成功回调
                        L.d(TAG, "startConfig  success");
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {
                    	//单个设备配网失败回调
                        L.d(TAG, "errorCode: " + errorCode + " errorMsg: " + errorMsg);
                    }

                    @Override
                    public void onFinish() {
                    	//所有设备配网结束回调
                        L.d(TAG, "subDevBean onFinish: ");             
                    }
                });
ITuyaBlueMeshActivator iTuyaBlueMeshActivator = TuyaHomeSdk.getTuyaBlueMeshConfig().newWifiActivator(tuyaBlueMeshActivatorBuilder);
iTuyaBlueMeshActivator.startActivator();

```

## 1.4 Mesh设备操作
ITuyaBlueMeshDevice类提供了对Mesh设备的操作

### Mesh设备判断方法
##### 【代码范例】

```
DeviceBean deviceBean=TuyaHomeSdk.getDataInstance().getDeviceBean(mDevId);
if(deviceBean.isBleMesh()){
    L.d(TAG, "This device is mesh device");
 }else{
 
}

```



### 大小类介绍
#####  Mesh产品目前分为五大类
	  灯大类(01): 1-5路RGBWC彩灯
	  电工类(02)：1-6路插座
	  传感器类(04)：门磁、PIR（传感类主要是一些周期性上报的传感数据）
	  执行器类(10)：马达、报警器之类用于执行的设备
	  适配器(08)：网关（带有mesh及其他通信节点的适配器）
#####  小类编号
	  1-5路灯（01-05）
	  1-6路排插（01-06)
	  .....

#####  举例
```
四路灯  		0401
五路插座		0502
......

```


###  控制指令下发
#####  【指令格式】
发送控制指令按照以下格式： {"(dpId)":"(dpValue)"}  

##### 【方法调用】
```
//控制设备
* @param nodeId   子设备本地编号
* @param pcc		  设备产品大小类
* @param dps		  控制指令
* @param callback  回调
void publishDps(String nodeId, String pcc, String dps, IResultCallback callback);


//控制群组
* @param localId   群组本地编号
* @param pcc		  产品大小类
* @param dps		  控制指令
* @param callback  回调
void multicastDps(String localId, String pcc, String dps, IResultCallback callback)
```

##### 【代码范例】
```
//设备控制
String dps = {"1":false};
ITuyaBlueMeshDevice mTuyaBlueMeshDevice=TuyaHomeSdk.newBlueMeshDeviceInstance("meshId");
mTuyaBlueMeshDevice.publishDps(devBean.getNodeId(), devBean.getCategory(), dps, new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
            		Toast.makeText(mContext, "发送失败"+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "发送成功", Toast.LENGTH_LONG).show();
            }
        });
        
        
//群组控制        
String dps = {"1":false};
ITuyaBlueMeshDevice mTuyaBlueMeshDevice= TuyaHomeSdk.newBlueMeshDeviceInstance("meshId");
mTuyaBlueMeshDevice.multicastDps(groupBean.getLocalId(), devBean.getCategory(), dps, new IResultCallback() {
            @Override
            public void onError(String errorCode, String errorMsg) {
            		Toast.makeText(mContext, "发送失败"+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "发送成功", Toast.LENGTH_LONG).show();
            }
        });

```
###  数据监听
##### 【描述】
mesh网内相关信息（dp数据、状态变更、设备名称、设备移除）会实时同步到 IMeshDevListener 

##### 【实现回调】
```
mTuyaBlueMeshDevice.registerMeshDevListener(new IMeshDevListener() {

		/**
         * 数据更新
         * @param nodeId    更新设备的nodeId
         * @param dps       dp数据
         * @param isFromLocal   数据来源 true表示从本地蓝牙  false表示从云端
         */
            @Override
            public void onDpUpdate(String nodeId, String dps,boolean isFromLocal) {
                //可以通过node来找到相对应的DeviceBean
                DeviceBean deviceBean = mTuyaBlueMeshDevice.getMeshSubDevBeanByNodeId(nodeId);
            }

		 /**
         * 设备状态的上报
         * @param online    在线设备列表
         * @param offline   离线设备列表
         * @param gwId      状态的来源 gwId不为空表示来自云端（gwId是上报数据的网关Id）   为空则表示来自本地蓝牙
         */
            @Override
            public void onStatusChanged(List<String> online, List<String> offline,String gwId) {

            }
            
        /**
         * 网络状态变化
         * @param devId
         * @param status
         */
            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }
            

        /**
         * raw类型数据上报
         * @param bytes
         */
            @Override
            public void onRawDataUpdate(byte[] bytes) {

            }
            

        /**
         * 设备信息变更（名称等）
         * @param bytes
         */            
            @Override
            public void onDevInfoUpdate(String devId) {

            }
            
        /**
         * 设备移除
         * @param devId
         */
            @Override
            public void onRemoved(String devId) {

            }
        });
```




### 创建群组
##### 【指令格式】
mesh网内支持创建 28672 个群组  返回时id范围 8000 ~ EFFF  （16进制）  由本地进行维护
#####  【方法调用】
```
* @param name			群组名称
* @param pcc			群组中设备的大小类  (支持跨小类创建  FF01 表示覆盖灯大类)
* @param localId		群组的localId  (范围 8000 ~ EFFF 16进制字符串)
* @param callback		回调
public void addGroup(String name, String pcc, String localId,IAddGroupCallback callback);
```

##### 【代码范例】
```
mITuyaBlueMesh.addGroup("群组名称","大小类", "8001", new IAddGroupCallback() {
			@Override
            public void onError(String errorCode, String errorMsg) {
            		Toast.makeText(mContext, "创建群组失败"+ errorMsg, Toast.LENGTH_LONG).show();
            }
            
            	
            @Override
            public void onSuccess(long groupId) {
            		Toast.makeText(mContext, "创建群组成功", Toast.LENGTH_LONG).show();
            }
        
        });
```

###  子设备重命名
##### 【方法调用】
```
* @param devId    	 设备Id
* @param name		  重命名名称
* @param callback	  回调
public void renameMeshSubDev(String devId, String name, IResultCallback callback);

```

##### 【代码范例】
```
 mTuyaBlueMesh.renameMeshSubDev(devBean.getDevId(),"设备名称", new IResultCallback() {
            @Override
            public void onError(String code, String errorMsg) {
            		Toast.makeText(mContext, "重命名失败"+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "重命名成功", Toast.LENGTH_LONG).show();
            }
        });
```

###  子设备移除
#####  【方法调用】
```
* @param devId    	 设备Id
* @param pcc  		 设备大小类
* @param callback	  回调
public void removeMeshSubDev(String devId, String pcc, IResultCallback callback) ;

```
#####  【代码范例】
```
mTuyaBlueMesh.removeMeshSubDev(devBean.getDevId(),devBean.getCategory(), new IResultCallback() {
            @Override
            public void onError(String code, String errorMsg) {
            		Toast.makeText(mContext, "子设备移除失败 "+ errorMsg, Toast.LENGTH_LONG).show();
    
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "子设备移除成功", Toast.LENGTH_LONG).show();
            }
        });
```

### 单个子设备信息查询
##### 【说明】
云端获取到的dp点数据可能不是当前设备实时的数据，可以通过该命令去查询设备的当前数据值，结果通过IMeshDevListener的onDpUpdate方法返回

#####  【方法调用】
```
* @param pcc  		 设备大小类
* @param nodeId    	 设备nodeId
* @param callback	  回调
public void querySubDevStatusByLocal(String pcc, final String nodeId, final IResultCallback callback);

```

#####  【代码范例】
```
 mTuyaBlueMeshDevice.querySubDevStatusByLocal(devBean.getCategory(), devBean.getNodeId(), new IResultCallback() {
            @Override
            public void onError(String code, String errorMsg) {
            		Toast.makeText(mContext, "查询失败 "+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "查询成功 ", Toast.LENGTH_LONG).show();
            }
        });
```



## 1.5 Mesh群组操作
ITuyaGroup类提供了对Mesh群组的操作
### Mesh群组判断方法

可以通过群组中是否具备MeshId来区分Mesh群组和普通wifi群组
#####  【代码范例】

```
GroupBean groupBean=TuyaHomeSdk.getDataInstance().getGroupBean("groupId");
if(!TextUtils.isEmpty(groupBean.getMeshId())){    
	L.d(TAG, "This group is mesh group");
}else{

}

```

### 添加子设备到群组

##### 【方法调用】
```
* @param devId		设备Id
* @param callback	回调
public void addDevice(String devId,IResultCallback callback);
```
##### 【代码范例】

```
ITuyaGroup mGroup = TuyaHomeSdk.newBlueMeshGroupInstance(groupId);
mGroup.addDevice("devId", new IResultCallback() {
            @Override
            public void onError(String code, String errorMsg) {
            		Toast.makeText(mContext, "添加设备到群组失败 "+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "添加设备到群组成功 ", Toast.LENGTH_LONG).show();
            }
        });
```


### 从群组中移除子设备
##### 【方法调用】
```
* @param devId		设备Id
* @param callback	回调
public void removeDevice(String devId,IResultCallback callback);

```

##### 【代码范例】
```
ITuyaGroup mGroup = TuyaHomeSdk.newBlueMeshGroupInstance(groupId);
mGroup.removeDevice("devId", new IResultCallback() {
            @Override
            public void onError(String code, String errorMsg) {
            		Toast.makeText(mContext, "从群组中移除设备失败 "+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "从群组中移除设备成功 ", Toast.LENGTH_LONG).show();
            }
        });

```

### 解散群组
##### 【方法调用】
```
* @param callback	回调
public void dismissGroup(IResultCallback callback);
```
##### 【代码范例】
```
ITuyaGroup mGroup = TuyaHomeSdk.newBlueMeshGroupInstance(groupId);
mGroup.dismissGroup(new IResultCallback() {
            @Override
            public void onError(String code, String errorMsg) {
            		Toast.makeText(mContext, "解散群组失败 "+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "解散群组成功 ", Toast.LENGTH_LONG).show();
            }
        });

```


### 重命名群组
##### 【方法调用】
```
* @param groupName	重命名名称
* @param callback	回调
public void renameGroup(String groupName,IResultCallback callback);
```
##### 【代码范例】
```
ITuyaGroup mGroup = TuyaHomeSdk.newBlueMeshGroupInstance(groupId);
mGroup.renameGroup("群组名称",new IResultCallback() {
            @Override
            public void onError(String code, String errorMsg) {
            		Toast.makeText(mContext, "重命名群组失败 "+ errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess() {
            		Toast.makeText(mContext, "重命名群组成功 ", Toast.LENGTH_LONG).show();
            }
        });

```


## 1.6 Mesh子设备升级
子设备升级分为2种，一种是普通设备升级，一种是mesh网关升级

### 子设备升级信息获取
```
TuyaHomeSdk.getMeshInstance().requestUpgradeInfo(mDevID, new IRequestUpgradeInfoCallback() {
    @Override
    public void onSuccess(ArrayList<BLEUpgradeBean> bleUpgradeBeans) {
    	for (BLEUpgradeBean bean : bleUpgradeBeans) {
             if (bean.getUpgradeStatus()==1) {
				if (bean.getType() == 0) {
					//wifi模块需要升级
				}else if(bean.getType == 1){
					//蓝牙模块需要升级
					//需要手动下载ota固件
					url=bean.getUrl()
				}
             }else{
                 //无需更新
             }
		 }
    }

    @Override
    public void onError(String errorCode, String errorMsg) {
    }
});
```

### 普通子设备升级
##### 【初始化参数配置】
```
TuyaBlueMeshOtaBuilder build = new TuyaBlueMeshOtaBuilder()
            .setData(byte[] data)
            .setMeshId(String meshId)
            .setProductKey(String productKey)
            .setNodeId(String nodeId)      
            .setDevId(String devId)
            .setVersion(String version)
            .setTuyaBlueMeshActivatorListener(MeshUpgradeListener mListener)
            .bulid();

```
##### 【参数说明】
###### 【入参】
```
* @param data     				待升级固件的字节流
* @param meshId   				设备MeshId
* @param productKey    		   设备产品Id
* @param mNodeId  				设备NodeId
* @param devId 					设备Id 
* @param version     			待升级固件的版本号
```

###### 【出参】
MeshUpgradeListener listener 升级回调接口

##### 【代码范例】
```
private MeshUpgradeListener mListener = new MeshUpgradeListener() {
        @Override
        public void onUpgrade(int percent) {
        	//升级进度
        }

        @Override
        public void onSendSuccess() {
        	//固件数据发送成功
        }

        @Override
        public void onUpgradeSuccess() {
        	//升级成功
        	 mMeshOta.onDestroy();
        }

        @Override
        public void onFail(String errorCode, String errorMsg) {
        	//升级失败
        	 mMeshOta.onDestroy();
        }
    };
//获取指定文件的字节流
byte data[] = getFromFile(path);

TuyaBlueMeshOtaBuilder build = new TuyaBlueMeshOtaBuilder()
        .setData(data)
        .setMeshId(mDevBean.getMeshId())
        .setProductKey(mDevBean.getProductId())
        .setNodeId(mDevBean.getNodeId())
        .setDevId(mDevID)
        .setVersion("version")
        .setTuyaBlueMeshActivatorListener(mListener)
        .bulid();
ITuyaBlueMeshOta  = TuyaHomeSdk.newMeshOtaManagerInstance(build);

//开始升级
mMeshOta.startOta();
```

### 网关设备升级
网关设备升级分为2步 1、升级蓝牙模块（操作同 普通子设备升级） 2、升级wifi模块

##### 【代码范例】
```
private IOtaListener iOtaListener = new IOtaListener() {
        @Override
        public void onSuccess(int otaType) {
            //升级成功
  		
  		}

        @Override
        public void onFailure(int otaType, String code, String error) {
         	//升级失败

        }

        @Override
        public void onProgress(int otaType, final int progress) {
           //升级进度
                      
        }
    };

ITuyaOta iTuyaOta = TuyaHomeSdk.newOTAInstance(mDevID);
iTuyaOta.setOtaListener(mOtaListener);
//开始升级
iTuyaOta.startOta();

//销毁升级
iTuyaOta.onDestroy();

```



