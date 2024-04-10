#!/bin/sh
            echo DEBIAN package for 6.00
            rm -r -f /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package
            mkdir /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package
            echo Copy debian-package structure
            cp -R /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/deployment/linux/debian/DEBIAN /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package
            cp -R /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/deployment/linux/debian/usr /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package
            sed -i -e "s/@@version@@/6.00/g" /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package/DEBIAN/control
            sed -i -e "s/@@version@@/6.00/g" /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/applications/ostorybook.desktop
            sed -i -e "s/(version)/(6.00)/g" /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/doc/ostorybook/changelog
            gzip -9 -n /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/doc/ostorybook/changelog
            echo Copy application
            
            cp /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/oStorybook.jar /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/ostorybook/oStorybook.jar
            echo Modification for permissions
            find /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package/usr -type d -exec chmod 755 {} +
            chmod -R 755 /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package/DEBIAN
            echo set the md5
            cd /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package
            rm -f ./DEBIAN/md5sums
            find . -type f -not -path "./DEBIAN/*" -exec md5sum {} + | sort -k 2 | sed 's/\.\/\(.*\)/\1/' > ./DEBIAN/md5sums
            echo build of Debian package
            dpkg-deb --build /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/oStorybook-6.00.deb
            echo Cleaning...
            rm -r -f /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/debian-package
            echo Build RPM package
            cd /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00
            alien -r /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/oStorybook-6.00.deb
            mv /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/ostorybook-6.00*.rpm oStorybook-6.00.rpm
            lintian -i -I /home/favdb/xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.00/oStorybook-6.00.deb
        