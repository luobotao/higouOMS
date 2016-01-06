package vo;


public class ImportNeolixVO{
    public String orderNum;//订单号
    public String mechantNum;//商户商品编号
    public String productName;	//商品名称
    public int counts;//商品数量
    public String price;//商品 单价
    public String totalFee;//总金额(不含运费)
    public String dateOrder;//订单日期 2015-04-21 17:57
    public String expenses;//运费
    public String datePay;//支付日期 2015-04-21 17:57
    public String receiver;//收货人
    public String parentsAddress;//收货人地址（北京北京市朝阳区，Excel文件中收货地址的前三截）
    public String subAddress;//收货人地址（北城新区齐鲁园西门，Excel文件中收货地址的前三截之后的地址）
    public String phone;//收货人电话
    //暂未用
   /* public String userName;//客户姓名
    public String cardId;//身份证号
    public String status;//状态 待发货
*/}
