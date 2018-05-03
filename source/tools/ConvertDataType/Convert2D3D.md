---
title: 二维与三维数据转换
---

　　类型转换提供了二维点、线、面数据与三维点、线、面数据互转的功能，本文将分别介绍二维数据转三维数据、三维数据转二维数据的内容。

### 二维数据TO三维数据
  
　　二维数据转换为三维数据时，需要设置相应的高程字段，转换为三维数据后，会保留二维数据集的字段和属性信息，并且对象的节点信息中会增加高程信息。将转换后的三维数据加载到场景中显示。将三维数据的添加到场景中，设置“高度模式”为非贴地模式，则三维数据对象会按照 在 z 坐标的数值显示该点的高度。



**操作说明** 
  
 1. 在工具箱的“二维数据与三维数据互转”选项中，选择需要的转换类型。

   - 转换点数据或面数据时，需要设置一个字段为高程字段，转换时会将该字段存储为Z值。
   - 二维线转三维线数据时，需要设置线的起始高程和终止高程字段。

 2. 在对话框中设置待转换的数据集，及结果数据集名称和所存的数据源即可。 



### 三维数据TO二维数据

　　三维点数据转二维点数据集时，会从属性表中删除 SMZ 字段，同时在节点信息中去掉 Z 坐标信息。三维线数据集和三维面数据集转换时，会将节点信息中的 Z 坐标信息去除。

**操作说明**

 1. 在工具箱中的“二维数据与三维数据互转”选项中，选择相应的三维数据转二维数据类型。
 2. 在弹出的对话框中设置待转的三维数据集，及结果数据即可。


