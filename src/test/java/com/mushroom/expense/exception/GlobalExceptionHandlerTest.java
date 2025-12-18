package com.mushroom.expense.exception;

import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleException_ReturnsErrorView() {
        Exception ex = new Exception("Test Error");
        Model model = mock(Model.class);

        String viewName = exceptionHandler.handleException(ex, model);

        assertEquals("error", viewName);
        verify(model).addAttribute("errorMessage", "Test Error");
    }

    @Test
    void handleMaxSizeException_ReturnsRedirect() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1000L);
        RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

        String viewName = exceptionHandler.handleMaxSizeException(ex, redirectAttributes);

        assertEquals("redirect:/dashboard", viewName);
        verify(redirectAttributes).addFlashAttribute("error", "File too large! Maximum upload size is 10MB.");
    }
}
