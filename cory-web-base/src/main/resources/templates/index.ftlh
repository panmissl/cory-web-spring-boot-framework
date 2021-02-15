<!doctype html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width,initial-scale=1.0">
        <!-- Tell the browser to be responsive to screen width -->
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
        <meta http-equiv="content-type" content="text/html; charset=utf-8">

        <script type="text/javascript" language="javascript">
            <#-- 一些从后台初始化的js数据可以放这里 -->
            const domainName = "${domainName}";
            const ctx = "${ctx}";
            const ctxWithoutSlash = "${ctxWithoutSlash}";
            const jsDir = "${jsDir}";
            const cssDir = "${cssDir}";
            const imageDir = "${imageDir}";
            const isAdmin = ${currentUser.isAdmin};
            const isRoot = ${currentUser.isRoot};
            const loginPage = ${loginPage!false};
            const registerPage = ${registerPage!false};
        </script>

        <title>${siteName}</title>

        <link rel="shortcut icon" href="${ctx }favicon.ico" type="image/x-icon" />
        <link rel="icon" href="${ctx }favicon.ico" type="image/x-icon" />

        <script src="${jsDir}lib/plugins/select2/select2.full.min.js"></script>
    </head>
    <body>
        <div>index page</div>
    </body>
</html>
