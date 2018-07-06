---
title: 距离栅格概述
---

　　距离栅格分析是对每一个栅格距离其邻近像元（源）的空间距离进行分析，从而反映每个像元到邻近像元的空间关系。不仅考虑栅格表面距离，还会考虑成本等各种耗费因素的影响。距离栅格分析功能包含了以下三方面：

 - 生成距离栅格，以及相应的方向栅格和分配栅格；
 - 最短路径分析，该功能是在生成距离栅格的基础上进行分析；
 - 计算两点（源点和目标点）间的最短路径，包括最小耗费路径、最短表面距离路径。

　　通过距离分析，可以获取很多有用信息，指导人们进行资源的管理和规划，例如：

 - 地震紧急救援震区到最近医院的距离；
 - 连锁超市的服务区域评估等。

### 基本概念


 - **源**：感兴趣的研究对象或地物，如学校、道路或消防栓等。
 - **源数据集**：包含源的数据集，可以为二维点、线、面数据集或栅格数据集。对于栅格数据集，要求除标识源以外的单元格为无值。
 - **距离**：包括直线距离、耗费距离和表面距离。

   - 直线距离即欧氏距离，是每一个单元格到最近源的直线距离；
   - 耗费距离是根据单元格的某个或某几个属性因子的加权得到的实际到达最近源所需的耗费值；
   - 表面距离即根据表面栅格计算出的单元格到达最近源的实际表面距离。直线距离可以看做是长短的耗费，是最简单的一种耗费。另一方面，实际的地表覆盖类型多样，通过直线距离来到达源几乎是不可能的，必须要绕道以避开如河流，高山等的障碍物，因而可以说耗费距离是对直线距离的扩展和延伸。

 - **耗费栅格**：生成耗费距离栅格以及计算最小耗费路径时都需要提供耗费栅格。耗费栅格用于确定经过每个单元格所需的成本。单元格的值表示经过此单元格时一个单位的耗费，不能为负值。例如一个表示不同地面环境中汽车前进阻力的耗费栅格，其栅格值代表经过该单元格时每前进1公里的阻力值，那么经过该单元格的总耗费为单位耗费值（即栅格值）乘以单元格大小。耗费栅格的单位可以是任何单位类型，如长度、时间、金钱等，也可以无单位，如重分级后的坡度、坡向、土地利用类型等。
  
   
### ![](../img/read.gif) 生成直线距离栅格

　　生成距离栅格功能用来计算栅格数据的每个像元与源数据的距离。获得的结果可以用来解决三个问题：

 - 栅格数据中每个像元到最近源数据的距离，例如到最近学校的距离。 
 - 栅格数据中每个像元到最近源数据的方向，例如到最近学校的方向。 
 - 根据资源的空间分布，要分配给源数据的像元，例如最近的几个学校位置。 

　　生成距离栅格会得到三种数据集，即距离栅格、方向栅格和分配栅格。如下图所示，分别展示了这三种栅格

 ![](img/CreateDis.png)  

　　实际应用中，直线距离栅格多用于经过的路线没有障碍或等同耗费的情况，例如，救援飞机飞往最近的医院时，空中没有障碍物，因此采用哪条路线的耗费均相同，此时通过直线距离栅格就可以确定从救援飞机所在地点到周围各医院的距离；根据直线分配栅格可以获知离救援飞机所在地点最近的医院；由直线方向栅格可以确定最近的医院在救援飞机所在地点的方位。然而，在救援汽车开往最近医院的实例中，因为地表有各种类型的障碍物，采用不同的路线的花费不尽相同，这时，使用直线距离就不能准确解决实际问题了。


   
**直线距离栅格**

　　直线距离栅格的值代表该单元格到最近的源的欧氏距离（即直线距离）。直线距离实际上可看作以长短作为耗费，是最简单的一种耗费。直线距离栅格不考虑耗费，即认为经过的路线没有障碍或等同耗费。生成直线距离栅格的源数据可以是矢量数据（点、线、面），也可以是栅格数据。分析的结果包含直线距离栅格数据集、直线方向栅格数据集和直线分配栅格数据集。

　　对于每个单元格，它的中心与源的中心相连的直线即为单元格到源的距离，计算的方法是通过二者形成的直角三角形的两条直角边来计算，因此直线距离的计算只与单元格大小（即分辨率）有关。下图为直线距离计算的示意图，其中源栅格的单元格大小（CellSize）为10。

　　![](img/StraightDistance.png)

 　　 那么第三行第三列的单元格到源的距离L为：

　　![](img/StraightDistanceFormula.png)


**直线方向栅格**

　　方向栅格表示了每个栅格像元与最近源之间的方位角方向。同样也可以分为直线方向栅格和耗费方向栅格。直线方向栅格的值表示该单元格到最近的源的方位角，单位为度。以正北方向为0度，正东方向为90度，正南为180度，正西为270度，正北为360度，顺时针方向旋转，范围为0-360度。例如，如果最近源在该单元格的正东方向，则该单元格的值为90度。

　　耗费方向栅格的值表达的是从该单元格到达最近的源的最小耗费路径的行进方向。如下图所示：

   - **图1**：用箭头标识出了每个单元格到达源（图中使用小红旗标识）的最小耗费路线；
   - **图2**：规定了路径方向的值，如：路径向左，则方向值为5，路径向上则方向值为7；
   - **图3**：为图1栅格数据每个单元格耗费方向栅格。对于源所在的单元格在耗费方向栅格数据集中的值为0。当然，方向数据集中栅格值为0的单元格也不都是源，例如，输入栅格数据集中为无值的单元格在输出的耗费方向数据集中的值也为0。

　　![](img/Distance.png)

**直线分配栅格**

　　用于标识每个单元格的最近源，其栅格值即为当前单元格的最近源的值，源为栅格时，为最近源的栅格值；源为矢量对象时，为最近源的SMID。分配栅格是将空间资源（栅格像元）分配给不同的源对象，例如可以表示多个邮局的服务区域，方便客户选择最近的邮局办理服务。

　　下图为生成直线距离栅格的示意图，单元格大小均为2。

　　![](img/StraightDistanceRaster.png)

### ![](../img/read.gif) 生成耗费距离栅格

　　直线距离是一种理想化的距离，实际应用中，往往不能满足要求。例如，从B到最近源A的直线距离与从C到最近源A的直线距离相同，若BA路段交通拥堵，而CA路段交通畅通，则其时间耗费必然不同；此外，通过直线距离对应的路径到达最近源时常常是不可行的，例如，遇到河流、高山等障碍物就需要绕行，这时就需要考虑其耗费距离。

　　耗费距离是根据每个单元格的某个或某几个属性因子的加权得到的到达最近源所需的实际耗费值，如耗费的时间、花费的资金等。生成耗费距离栅格功能根据源数据集和耗费栅格生成相应的耗费距离栅格、耗费方向栅格和耗费分配栅格。

　　![](img/CostResult.png)

**耗费距离栅格**

　　用于表达每个单元格到达最近源的最小耗费，其栅格值即为当前单元格到达最近源的耗费值。最近源是从当前单元格到达所有的源中耗费最小的一个源。对于耗费栅格中的无值栅格，计算距离结果时仍然为无值。

　　单元格到达源的耗费的计算方法是，从待计算单元格的中心出发，到达最近源的最小耗费路径在每个单元格上经过的距离乘以耗费栅格上对应单元格的值，将这些值累加即为单元格到源的耗费值。因此，耗费距离的计算与单元格大小和耗费栅格有关。在下图所示的示意图中，源栅格和耗费栅格的单元格大小（CellSize）均为2，单元格（2,1）到达源（0,0）的最小耗费路线如右图中红线所示：

　　![](img/CostRaster.png)


　　那么单元格（2,1）到达源的最小耗费（即耗费距离）为：

　　![](img/CostFormula.png)

**耗费方向栅格**

　　用于表达每个单元格到达最近源的最小耗费路径的行进方向。在耗费方向栅格中，可能的行进方向共有八个（正北、正南、正西、正东、西北、西南、东南、东北），使用1到8八个整数对这八个方向进行编码，如下图所示。注意，源所在的单元格在耗费方向栅格中的值为0，耗费栅格中为无值的单元格在输出的耗费方向栅格中将被赋值为15。 下图为耗费方向编码：

　　![](img/CostDistance.png)


**耗费分配栅格**

　　用于标示每个单元格的最近源，其栅格值即为当前单元格的最近源的值，源为栅格时，为最近源的栅格值；源为矢量对象时，为最近源的SMID。耗费栅格中为无值的单元格在输出的耗费分配栅格中仍为无值。

　　下图为生成耗费距离的示意图。单元格大小均为2。其中，在耗费栅格上，使用蓝色箭头标示了单元格到达最近源的行进路线，耗费方向栅格的值即为当前单元格到达最近源的最小耗费路线的行进方向。

　　![](img/CostRaster1.png)


### ![](../img/read.gif) 计算最短路径

　　计算最短路径就是根据目标数据集、耗费距离栅格和耗费方向栅格，计算目标到最近源的最短路径。

　　例如，分析从各居民小区（面数据集）出发，如何到达最近的购物商场（点数据集）。首先将购物商场作为源，生成耗费离栅格和耗费方向栅格；将居民小区作为目标区域，根据生成的耗费距离栅格和耗费方向栅格进行最短路径分析，就可以得到各居民小区（目标）到最近购物商场（源）的最短路径。

　　最短路径有三种类型，分别为像元路径、区域路径和单一路径：

 - **像元路径**：每一个栅格像元都生成一条路径，即每个目标像元到最近源的距离。如下图所示，红色点作为源，黑线框多边形作为目标，采用该方式进行栅格最短路径分析，得到蓝色单元格表示的最短路径。

　　![](img/CellType.png)

 - **区域路径**：每个栅格区域都生成一条路径，此处栅格区域指栅格值相等的连续栅格，区域路径即每个目标区域到最近源的最短路径。 如下图所示，红色点作为源，黑线框多边形作为目标，采用该方式进行栅格最短路径分析，得到蓝色单元格表示的最短路径。

　　![](img/ZoneType.png)

 - **单一路径**：所有像元只生成一条路径，即对于整个目标区域数据集来说所有路径中最短的一条。 如下图所示，红色点作为源，黑线框多边形作为目标，采用该方式进行栅格最短路径分析，得到蓝色单元格表示的最短路径。

　　![](img/AllType.png)

### ![](../img/read.gif) 两点间最短路径

　　计算源点和目标点两个点之间的最短路径。根据指定的表面栅格或耗费栅格，可以计算出两点间的最短表面距离路径、最小耗费路径或考虑表面距离的最小耗费路径。


### ![](../img/read.gif) 最小耗费路径

　　计算最小耗费路径需要指定耗费栅格。耗费栅格用于确定经过每个单元格所需的成本。单元格的值表示经过此单元格时一个单位的耗费。例如一个表示不同地面环境中汽车前进阻力的耗费栅格，其栅格值代表经过该单元格时每前进1公里的阻力值，那么经过该单元格的总耗费为单位耗费值（即栅格值）乘以单元格大小。耗费栅格的单位可以是任何单位类型，如长度、时间、金钱等，也可以无单位，如重分级后的坡度、坡向、土地利用类型等。通常，一个实际研究涉及到的成本影响因素可能有多个，例如，规划一条新的道路，影响成本的因素可能包括修建的总长度、经过的地区的土地利用类型、坡度、距离人口聚集区的距离等，那么分析时，就需要对这些因素进行加权从而得到一个综合权重作为耗费数据。此外，需注意耗费并不能为负值。

　　![](img/PointCostPath.png)


### 相关主题  

　　![](../img/smalltitle.png) [生成距离栅格](CreateRasterDistance.html)
 
　　![](../img/smalltitle.png) [计算最短路径](ShortPath.html) 
   
　　![](../img/smalltitle.png) [两点最短地表路径](TwoPointDis.html) 
   
　　![](../img/smalltitle.png) [两点最短耗费路径](TwoPointCostDis.html)    
 
