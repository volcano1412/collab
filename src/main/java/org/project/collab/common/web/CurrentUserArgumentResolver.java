package org.project.collab.common.web;
import org.project.collab.common.error.BusinessException; import org.project.collab.common.error.ErrorCode; import org.project.collab.user.repository.UserRepository;
import org.springframework.core.MethodParameter; import org.springframework.stereotype.Component; import org.springframework.web.bind.support.WebDataBinderFactory; import org.springframework.web.context.request.NativeWebRequest; import org.springframework.web.method.support.HandlerMethodArgumentResolver; import org.springframework.web.method.support.ModelAndViewContainer;
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository users; public CurrentUserArgumentResolver(UserRepository users){this.users=users;}
    public boolean supportsParameter(MethodParameter p){return p.hasParameterAnnotation(CurrentUser.class)&&p.getParameterType()==Long.class;}
    public Object resolveArgument(MethodParameter p,ModelAndViewContainer m,NativeWebRequest r,WebDataBinderFactory b){
        String raw=r.getHeader("X-User-Id"); final long id;
        try { if(raw==null||raw.isBlank()) throw new NumberFormatException(); id=Long.parseLong(raw); } catch(NumberFormatException e){throw new BusinessException(ErrorCode.MISSING_USER_HEADER);}
        if(!users.existsById(id)) throw new BusinessException(ErrorCode.USER_NOT_FOUND); return id;
    }
}
