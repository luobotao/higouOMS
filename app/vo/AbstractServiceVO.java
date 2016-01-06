package vo;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;
import utils.ErrorCode;

/**
 * @author luobotao
 * Date: 2015年4月16日 上午11:56:35
 * @param <T>
 */
public abstract class AbstractServiceVO<T extends AbstractServiceVO> implements ServiceVO {

    protected boolean success = true;
    protected String errorCode;
    protected String errorMsg;

    public static DefaultServiceVO createError(String errorKey){
        DefaultServiceVO vo = new DefaultServiceVO();
        vo.success = false;
        vo.errorCode = ErrorCode.getErrorCode(errorKey);
        vo.errorMsg = ErrorCode.getErrorMsg(errorKey);
        return vo;
    }

    public T error(String errorKey) {
        this.success = false;
        this.errorCode = ErrorCode.getErrorCode(errorKey);
        this.errorMsg = ErrorCode.getErrorMsg(errorKey);
        return (T) this;
    }

    public T error(AbstractServiceVO<?> vo) {
        this.success = false;
        this.errorCode = vo.getErrorCode();
        this.errorMsg = vo.getErrorMsg();
        return (T) this;
    }

    public T error(String errorKey, String extMsg) {
        this.success = false;
        this.errorCode = ErrorCode.getErrorCode(errorKey);
        this.errorMsg = ErrorCode.getErrorMsg(errorKey, extMsg);
        return (T) this;
    }

    public T systemError() {
        return error("global.systemError");
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean errorKeyEquals(String errorKey) {
        if (StringUtils.isBlank(errorCode)) {
            return false;
        }

        String inputErrorCode = ErrorCode.getErrorCode(errorKey);
        if (StringUtils.isBlank(inputErrorCode)) {
            return false;
        }

        return inputErrorCode.equals(errorCode);
    }

    public JsonNode toJson(){
        return Json.toJson(this);
    }
}
