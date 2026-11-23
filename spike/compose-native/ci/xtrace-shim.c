/* Diagnostic only: LD_PRELOAD shim that makes Xlib synchronous and logs every window create/destroy
 * and every XDeleteProperty with a backtrace, so the call that raises BadWindow on exit is the one
 * printed last before the X error. */
#define _GNU_SOURCE
#include <X11/Xlib.h>
#include <dlfcn.h>
#include <execinfo.h>
#include <stdio.h>
#include <string.h>

static void trace(const char *what, unsigned long id, const char *extra) {
    void *frames[24];
    int n = backtrace(frames, 24);
    fprintf(stderr, "XTRACE %s id=0x%lx %s\n", what, id, extra ? extra : "");
    for (int i = 1; i < n; i++) {
        Dl_info info;
        if (dladdr(frames[i], &info) && info.dli_fname)
            fprintf(stderr, "XTRACE   #%d %s +0x%lx (%s)\n", i, info.dli_fname,
                    (unsigned long)((char *)frames[i] - (char *)info.dli_fbase),
                    info.dli_sname ? info.dli_sname : "?");
    }
    fflush(stderr);
}

Display *XOpenDisplay(const char *name) {
    Display *(*real)(const char *) = dlsym(RTLD_NEXT, "XOpenDisplay");
    Display *d = real(name);
    if (d) XSynchronize(d, True);
    fprintf(stderr, "XTRACE XOpenDisplay -> %p (synchronous)\n", (void *)d);
    return d;
}

Window XCreateWindow(Display *d, Window p, int x, int y, unsigned w, unsigned h, unsigned bw, int depth,
                     unsigned cls, Visual *v, unsigned long mask, XSetWindowAttributes *a) {
    Window (*real)(Display *, Window, int, int, unsigned, unsigned, unsigned, int, unsigned, Visual *,
                   unsigned long, XSetWindowAttributes *) = dlsym(RTLD_NEXT, "XCreateWindow");
    Window win = real(d, p, x, y, w, h, bw, depth, cls, v, mask, a);
    trace("XCreateWindow", win, NULL);
    return win;
}

int XDestroyWindow(Display *d, Window w) {
    int (*real)(Display *, Window) = dlsym(RTLD_NEXT, "XDestroyWindow");
    trace("XDestroyWindow", w, NULL);
    return real(d, w);
}

int XDeleteProperty(Display *d, Window w, Atom prop) {
    int (*real)(Display *, Window, Atom) = dlsym(RTLD_NEXT, "XDeleteProperty");
    char *name = XGetAtomName(d, prop);
    trace("XDeleteProperty", w, name);
    if (name) XFree(name);
    return real(d, w, prop);
}
