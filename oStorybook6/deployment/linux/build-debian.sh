#!/bin/sh
            echo DEBIAN package for 6.00
            rm -r -f /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package
            mkdir /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package
            echo Copy debian-package structure
            cp -R /ext2/disk3/oStorybook/ostorybook6/oStorybook6/deployment/linux/debian/DEBIAN /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package
            cp -R /ext2/disk3/oStorybook/ostorybook6/oStorybook6/deployment/linux/debian/usr /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package
            sed -i -e "s/@@version@@/6.00/g" /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/DEBIAN/control
            sed -i -e "s/@@version@@/6.00/g" /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/applications/ostorybook.desktop
            sed -i -e "s/(version)/(6.00)/g" /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/doc/ostorybook/changelog
            gzip -9 -n /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/doc/ostorybook/changelog
            echo Copy application
            cp -R /ext2/disk3/oStorybook/ostorybook6/oStorybook6/Assistant /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/ostorybook/
            cp /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/oStorybook.jar /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/usr/share/ostorybook/oStorybook.jar
            echo Modification for permissions
            find /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/usr -type d -exec chmod 755 {} +
            chmod -R 755 /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package/DEBIAN
            echo set the md5
            cd /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package
            rm -f ./DEBIAN/md5sums
            find . -type f -not -path "./DEBIAN/*" -exec md5sum {} + | sort -k 2 | sed 's/\.\/\(.*\)/\1/' > ./DEBIAN/md5sums
            echo build of Debian package
            dpkg-deb --build /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/debian-package /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/oStorybook-6.00.deb
            echo Cleaning...
            
            echo Build RPM package
            cd /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00
            alien -r /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/oStorybook-6.00.deb
            mv /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/ostorybook-6.00*.rpm oStorybook-6.00.rpm
            lintian -i -I /ext2/disk3/oStorybook/ostorybook6/oStorybook6/distrib/6.00/oStorybook-6.00.deb
        