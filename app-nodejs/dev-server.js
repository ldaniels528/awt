/**
 * AWT ScalaJS Development Server Bootstrap
 * @author: lawrence.daniels@gmail.com
 */
(function () {
    require("./target/scala-2.11/awt-nodejs-fastopt.js");
    const facade = com.microsoft.awt.AWTServerJsApp();
    facade.startServer({
        "__dirname": __dirname,
        "__filename": __filename,
        "exports": exports,
        "module": module,
        "require": require
    });
})();