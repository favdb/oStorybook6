#!/bin/sh
            echo DEBIAN package for 6.03
            rm -r -f /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package
            mkdir /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package
            echo Copy debian-package structure
            cp -R /xDev/oStorybook/oStorybook6/oStorybook6/deployment/linux/debian/DEBIAN /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package
            cp -R /xDev/oStorybook/oStorybook6/oStorybook6/deployment/linux/debian/usr /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package
            sed -i -e "s/@@version@@/6.03/g" /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/DEBIAN/control
            sed -i -e "s/@@version@@/6.03/g" /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/usr/share/applications/ostorybook.desktop
            sed -i -e "s/(version)/(6.03)/g" /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/usr/share/doc/ostorybook/changelog
            gzip -9 -n /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/usr/share/doc/ostorybook/changelog
            echo Copy application
            
            mkdir /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/usr/share/ostorybook
            cp /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/oStorybook.jar /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/usr/share/ostorybook/oStorybook.jar
            echo Modification for permissions
            find /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/usr -type d -exec chmod 755 {} +
            chmod -R 755 /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package/DEBIAN/usr/bin
            echo set the md5
            cd /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package
            rm -f ./DEBIAN/md5sums
            find . -type f -not -path "./DEBIAN/*" -exec md5sum {} + | sort -k 2 | sed 's/\.\/\(.*\)/\1/' > ./DEBIAN/md5sums
            echo build of Debian package
            dpkg-deb --build /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/oStorybook-6.03.deb
            echo Cleaning...
            
            echo Build RPM package
            cd /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03
            alien -r /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/oStorybook-6.03.deb
            mv /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/ostorybook-6.03*.rpm oStorybook-6.03.rpm
            lintian -i -I /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/oStorybook-6.03.deb
            rm -r -f /xDev/oStorybook/oStorybook6/oStorybook6/distrib/6.03/debian-package
        