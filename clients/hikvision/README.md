# 海康门禁

门禁接口文档：https://open.hikvision.com/docs/docId?productId=5c67f1e2f05948198c909700&version=%2Ff95e951cefc54578b523d1738f65f0a1&tagPath=API%E5%88%97%E8%A1%A8-%E4%B8%80%E5%8D%A1%E9%80%9A%E5%BA%94%E7%94%A8%E6%9C%8D%E5%8A%A1-%E9%97%A8%E7%A6%81%E7%AE%A1%E7%90%86#a1ff98ef

## 查询门禁点事件v2

- 接口说明

    -
  功能描述：该接口可以查询发生在门禁点上的人员出入事件，支持多个维度来查询，支持按时间、人员、门禁点、事件类型四个维度来查询；其中按事件类型来查询的方式，如果查询不到事件，存在两种情况，一种是该类型的事件没有发生过，所以查询不到，还有一种情况，该类型的事件发生过，但是由于门禁管理组件对该事件类型订阅配置处于关闭状态，所以不会存储该类型的事件，导致查询不到，对于这种情况，需要到门禁管理组件中，将该事件类型的订阅配置打开。

- 接口适配产品版本

    - 综合安防管理平台iSecure Center V1.4及以上版本

- 接口版本

    - v2

- 接口地址

    - /api/acs/v2/door/events

- 请求方法

    - POST

- 数据提交方式

    - application/json

- 请求参数

| 参数名称                 | 数据类型     | 是否必须  | 参数描述                                                                                                                    |
|----------------------|----------|-------|-------------------------------------------------------------------------------------------------------------------------|
| pageNo               | integer  | True  | 当前页码（pageNo>0）                                                                                                          |
| pageSize             | integer  | True  | 每页展示数目（0<pageSize<=1000）                                                                                                |
| doorIndexCodes       | string[] | False | 门禁点唯一标识数组，最大支持10个门禁点，查询门禁点列表v2接口获取返回报文中的doorIndexCode字段                                                                 |
| doorName             | string   | False | 门禁点名称，支持模糊查询，从查询门禁点列表v2接口获取返回报文中的name字段                                                                                 |
| readerDevIndexCodes  | string[] | False | 读卡器唯一标识数组，最大支持50个读卡器，查询门禁读卡器列表接口获取返回报文中的indexCode字段                                                                     |
| startTime            | string   | False | 开始时间（事件开始时间，采用ISO8601时间格式，与endTime配对使用，不能单独使用，时间范围最大不能超过3个月，与receiveStartTime和receiveEndTime两者二选一为必填），参考附录ISO8601时间格式说明 |
| endTime              | string   | False | 结束时间（事件结束时间，采用ISO8601时间格式，最大长度32个字符，与startTime配对使用，不能单独使用，时间范围最大不能超过3个月），参考附录ISO8601时间格式说明                              |
| receiveStartTime     | string   | False | 入库开始时间，采用ISO8601时间格式，与receiveEndTime配对使用，不能单独使用，时间范围最大不能超过3个月，与startTime和endTime两者二选一为必填，参考附录ISO8601时间格式说明              |
| receiveEndTime       | string   | False | 入库结束时间，采用ISO8601时间格式，最大长度32个字符，与receiveStartTime配对使用，不能单独使用，时间范围最大不能超过3个月，参考附录ISO8601时间格式说明                             |
| doorRegionIndexCodes | string[] | False | 门禁点所在区域集合，查询区域列表v2接口获取返回参数indexCode，最大支持500个区域                                                                          |
| eventTypes           | number[] | False | 事件类型，参考附录D2.1 门禁事件                                                                                                      |
| personIds            | string[] | False | 人员数组（最大支持100个人员）                                                                                                        |
| personName           | string   | False | 人员姓名(支持中英文字符，不能包含 ’ / \ : * ? " < >                                                                                     |
| sort                 | string   | False | 排序字段（支持personName、doorName、eventTime填写排序的字段名称）                                                                          |
| order                | string   | False | 升/降序（指定排序字段是使用升序（asc）还是降序（desc）                                                                                         |

- 请求参数举例

```json
{
  "pageNo": 1,
  "pageSize": 10,
  "doorIndexCodes": [
    "1f276203e5234bdca08f7d99e1097bba"
  ],
  "doorName": "11",
  "readerDevIndexCodes": [
    "1f2762v4523547374"
  ],
  "startTime": "2018-05-21T12:00:00+08:00",
  "endTime": "2018-05-21T12:00:00+08:00",
  "receiveStartTime": "2018-05-21T12:00:00+08:00",
  "receiveEndTime": "2018-05-21T12:00:00+08:00",
  "doorRegionIndexCodes": [
    "c654234f-61d4-4dcd-9d21-e7a45e0f1334"
  ],
  "eventTypes": [
    10
  ],
  "personName": "xx",
  "sort": "personName",
  "order": "asc"
}
```

- 返回参数

| 参数名称                  | 数据类型     | 是否必须  | 参数描述                                        |
|-----------------------|----------|-------|---------------------------------------------|
| code                  | string   | False | 返回码，0-成功，其它参考附录E.2.1门禁管理错误码                 |
| msg                   | string   | False | 返回描述                                        |
| data                  | object   | False | 返回数据                                        |
| +pageNo               | number   | False | 当前页码                                        |
| +pageSize             | number   | False | 单页展示数据数目                                    |
| +total                | number   | False | 总结果数                                        |
| +totalPage            | number   | False | 总页数                                         |
| +list                 | object[] | False | 返回数据集合                                      |
| ++eventId             | string   | False | 事件ID，唯一标识这个事件                               |
| ++eventName           | string   | False | 事件名称                                        |
| ++eventTime           | string   | False | 事件产生时间，参考附录ISO8601时间格式说明                    |
| ++cardNo              | string   | False | 卡号                                          |
| ++personId            | string   | False | 人员唯一编码                                      |
| ++personName          | string   | False | 人员名称                                        |
| ++orgIndexCode        | string   | False | 人员所属组织编码                                    |
| ++orgName             | string   | False | 人员所属组织名称                                    |
| ++doorIndexCode       | string   | False | 门禁点编码                                       |
| ++doorName            | string   | False | 门禁点名称                                       |
| ++doorRegionIndexCode | string   | False | 门禁点所在区域编码                                   |
| ++picUri              | string   | False | 抓拍图片地址，通过接口获取门禁事件的图片接口获取门禁事件的图片数据           |
| ++svrIndexCode        | string   | False | 图片存储服务的唯一标识                                 |
| ++eventType           | number   | False | 事件类型，参考附录D2.1门禁事件                           |
| ++inAndOutType        | number   | False | 进出类型(1：进0：出-1:未知要求：进门读卡器拨码设置为1，出门读卡器拨码设置为2) |
| ++readerDevIndexCode  | string   | False | 读卡器IndexCode                                |
| ++readerDevName       | string   | False | 读卡器名称                                       |
| ++devIndexCode        | string   | False | 控制器设备IndexCode                              |
| ++devName             | string   | False | 控制器名称                                       |
| ++identityCardUri     | string   | False | 身份证图片uri，它是一个相对地址，可以通过获取门禁事件的图片接口，获取到图片的数据  |
| ++receiveTime         | string   | False | 事件入库时间，参考附录ISO8601时间格式说明                    |
| ++jobNo               | string   | False | 工号                                          |
| ++studentId           | string   | False | 学号                                          |
| ++certNo              | string   | False | 证件号码                                        |

- 返回参数举例

```json
{
  "code": "0",
  "msg": "success",
  "data": {
    "total": 1,
    "totalPage": 1,
    "pageNo": 1,
    "pageSize": 100,
    "list": [
      {
        "eventId": "207dd3b1-37a7-4d6c-8e4d-c8bfd343051b",
        "eventName": "acs.acs.eventType.successCard",
        "eventTime": "2019-11-16T15:44:33+08:00",
        "personId": "216e2ba145824269a1cbb423cdc85cb1",
        "cardNo": "3891192334",
        "personName": "sdk人员1zzzcb",
        "orgIndexCode": "root000000",
        "orgName": "默认组织",
        "doorName": "10.40.239.69new_test2_门_1",
        "doorIndexCode": "f0b50050d3434f15b4e34f885d5dacfe",
        "doorRegionIndexCode": "fd2df06b-1afb-4c9b-b058-5740c2c00076",
        "picUri": "no-pcnvr",
        "svrIndexCode": "/pic?=d62i7f6e*6a7i125-c838b9--a8c67dea96e65icb1*=sd*=5dpi*=1dpi*m2i1t=4ed35444bb4s=-39",
        "eventType": 198914,
        "inAndOutType": 1,
        "readerDevIndexCode": "378e563bf3e84d5ba6ef5742bbaa8933",
        "readerDevName": "读卡器_1",
        "devIndexCode": "dcff422aad9c4d60a47b8b2fe2757b71",
        "devName": "10.40.239.69new_test2",
        "identityCardUri": "/pic?=d62i7f6e*6a7i125-c838b9--a8c67dea96e65icb1*=sd*=5dpi*=1dpi*m2i1t=4ed35444bb4s=-39z422d3",
        "receiveTime": "2019-11-16T15:45:13.525+08:00",
        "jobNo": "23333",
        "studentId": "201900001",
        "certNo": "320826199012110005"
      }
    ]
  }
}
```

## ISO8601时间格式说明

ISO8601是国际标准化组织制定的日期时间表示规范，全称是《数据存储和交换形式·信息交换·日期和时间的表示方法》

合并表示时，要在时间前面加一大写字母T，如要表示北京时间2004年5月3日下午5点30分8秒，可以写成2004-05-03T17:
30:08+08:00或20040503T173008+08。

