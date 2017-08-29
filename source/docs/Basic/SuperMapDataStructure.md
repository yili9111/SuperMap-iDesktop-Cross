---
title: SuperMap 数据组织结构
---

　　SuperMap iDesktop Cross 的数据组织结构，主要包括工作空间、数据源、数据集、地图和场景。数据组织形式为类似于树状层次结构，一个工作空间包含的数据源集合、地图集合、场景集合和符号资源集合。

## 工作空间

　　工作空间是用户进行地理操作的工作环境，用户在进行数据操作时，都需要先创建一个工作空间。应用程序启动时，默认为用户建立了一个空的工作空间，用户可以在此基础上进行数据操作，或者打开已有工作空间进行操作。工作空间会保存用户在该工作环境中的操作结果，包括用户在该工作空间中打开的数据源、保存的地图、布局和三维场景等，当用户打开工作空间时可以基于上一次的工作成果继续工作。



### 工作空间类型

　　按照工作空间的存储形式，工作空间可以分为文件型、数据库型两类。文件型工作空间以 *.smwu 或*.sxwu 两种格式进行存储；数据库型工作空间，是将工作空间保存在数据库中，目前仅支持存储在 Oracle 和 SQL Server 数据库中（Linux平台中不支持SQL Server类型的工作空间）。

### 工作空间管理

　　工作空间中的地图、三维场景和资源都是保存在工作空间中，删除工作空间时，其中的地图、三维场景和符号库资源也相应的随之删除；而数据源是独立存储的，与工作空间只是关联关系，并没有保存在工作空间中，当删除工作空间时，只是删除了工作空间与数据源的关联关系，并不能删除数据源。工作空间的管理包括工作空间的打开、保存、另存、关闭和删除等内容。

* **打开**：若打开的工作空间为文件型工作空间，在工具栏中单击“打开工作空间”下拉菜单中的“文件型...”选项，选择需打开的文件型工作空间即可；若需打开数据库型工作空间，则选择“Oracle...”（或“SQL Server...”）选项，在弹出的对话框中设置服务器地址、数据库名称、用户名信息、工作空间名称等参数，即可打开指定的工作空间。

* **保存、另存**：用于保存工作空间中的工作环境及操作结果，同时可选择是否保存工作空间中的地图和场景。另存工作空间可将工作空间保存为文件型、Oracle 型或 SQL Server型。

* **关闭**：工作空间节点右键菜单中，提供了“关闭工作空间”功能，若应用程序中当前打开的工作空间没有未被保存的内容，则直接关闭当前的工作空间；如果当前打开的工作空间存在未被保存的内容，则会弹出对话框，提示用户在关闭当前打开的工作空间时是否保存这些内容。

## 数据源

　　SuperMap iDesktop Cross 的空间数据可以存储在文件中和数据库中，即数据源可以保存在文件中或者数据库中。数据源可以分为：文件型数据源、数据库型数据源、 Web 数据源、内存数据源四种。

### 数据源类型

* **文件型数据源**：即 UDB 类型数据源，存储于扩展名为 *.udb/*.udd 的文件中。新建 UDB 数据源时，会同时产生两个文件，*.udb 文件和与之相对应的 *.udd 文件，且这两个文件名除后缀名部分相同。 GIS 空间数据除了包含空间几何对象外，还包含对象的属性信息，在文件型数据源中， *.udb 文件主要存储空间数据的空间几何信息，*.udd 文件存储属性信息。UDB 数据源，是一个跨平台、支持海量数据高效存取的文件型数据源，UDB 可以存储的数据上限达到 128TB 大小。

* **数据库型数据源**：存储于数据库中，如 Oracle Plus 数据库、SQL Server Plus 数据库等（Linux平台中不支持SQL Server类型的数据源）。对应数据库型数据源，其空间数据的空间几何信息和属性信息都存储在数据库中。要对数据源中的空间数据操作，必须先通过工作空间中的数据源集合打开数据源，并且对数据源及其中的空间数据的所有操作将直接保存在数据源中而不是保存在工作空间中。数据源是独立于工作空间存储的，删除工作空间本身，工作空间中的数据源不会随之删除和变化。

* **Web 数据源**：存储于网络服务器上的数据源，如 OGC 数据源、iServerREST 数据源、超图云服务数据源、GoogleMaps 数据源、百度地图数据源、天地图服务数据源。在使用该类型的数据源时，通过 URL 地址来获取相应的数据源。

* **内存数据源**：内存数据源为临时数据源，只支持新建不支持保存，但是在内存数据源中进行数据处理效率较高。

### 数据源管理

　　数据源用于存储空间数据，独立于工作空间，用户可根据数据的用途，将不同的空间数据保存于数据源中，对这些数据统一进行管理和操作。SuperMap iDesktop Cross 提供的数据源管理有打开、新建、查看属性、重命名、关闭等操作。

* **新建数据源**：数据源结点右键菜单和工具栏中提供了新建数据源功能，支持新建文件型、数据库型、内存数据源三种类型，其中数据库型数据源支持SQLPlus（Linux平台中不支持）、OraclePlus、OracleSpatial、PostgreSQL、DB2、MySQL 等数据引擎。

* **打开数据源**：数据源结点右键菜单和工具栏中提供了打开数据源功能，支持打开的数据源类型有文件型、数据库型、Web型三种类型数据源，同时支持将外部矢量和影像文件以数据源形式打开。若打开的是文件型数据源，则选择指定的UDB文件或影像、矢量文件即可打开；若打开的是数据库型数据源，则需设置数据源所在的服务器地址、数据库名称、用户信息等参数，即可打开指定的数据库型数据源；若打开的是Web型数据源，则输入Web数据源路径即可打开。

* **数据源属性**：数据源右键菜单中提供了数据源属性功能，可通过“属性”对话框查看数据源的属性信息和投影信息。

## 数据集

　　数据集用于存储地理空间对象，是 SuperMap GIS 空间数据的基本组织单位之一。数据集的管理包括数据集的新建、复制、导入、导出、删除、关闭、重命名等操作，也包括查看数据的属性、设置数据集的投影等。目前支持二三维点、线、面数据集、纯属性数据集、网络数据集、复合数据集、文本数据集、路由数据集、影像/栅格数据集、模型数据集等多种类型。

* **新建数据集**：数据源支持新建的数据集类型有：二三维点、线、面数据集、文本、CAD、属性表等多种类型的数据集，在“新建数据集”对话框中设置目标数据源、创建类型、数据集名称、编码类型等参数后，即可创建用户所需的数据集。

* **复制数据集**：即将一个或者多个数据集复制到目标数据源中，可通过右键菜单功能复制，也可将选中数据集直接拖拽到目标数据源实现复制。

* **导入导出数据集**：SuperMap 除了支持自己的数据类型外，还支持多种外部的数据的导入；也支持将数据导出为其他软件支持的数据格式，具体说明请参见数据导入和数据导出页面。
* **影像金字塔**：影像金字塔技术是处理海量影像常常采用的技术，对栅格、影像数据集创建影像金字塔后，可提高数据的浏览速度。关于影像金字塔的具体说明请参见[影像金字塔](Pyramid.html)页面。
