package vo;

import java.util.LinkedList;
import java.util.List;

/**
 * @author luobotao
 * Date: 2015年4月16日 上午11:56:51
 */
public class CloseTaskVO extends AbstractServiceVO<CloseTaskVO> {
    private List<Item> list = new LinkedList<>();

    public List<Item> getList() {
        return list;
    }

    public void addItem(Long id, Integer status) {
        list.add(new Item(id, status));
    }

    public static class Item{
        private Long id;
        private String name;
        private Integer status;

        public Item(){
        	super();
        }
        
        public Item(Long id, Integer status) {
            this.id = id;
            this.status = status;
        }
        
        public Item(Long id, String name, Integer status) {
			super();
			this.id = id;
			this.name = name;
			this.status = status;
		}


		public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
        
        
    }
}
