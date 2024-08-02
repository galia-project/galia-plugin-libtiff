# LibTIFF Plugin for Galia

Provides LibTIFFDecoder.

See the [LibTIFF Plugin page on the website](https://galia.is/plugins/libtiff/)
for more information.

## Development

The native binding to libtiff was generated using jextract 22:

```sh
jextract --target-package org.libtiff \
    --output /path/to/src/main/java \
    /path/to/include/tiff.h
jextract --target-package org.libtiff \
    --output /path/to/src/main/java \
    /path/to/include/tiffio.h
jextract --target-package org.libtiff \
    --output /path/to/src/main/java \
    /path/to/include/tiffconf.h
jextract --target-package org.libtiff \
    --output /path/to/src/main/java \
    /path/to/include/tiffvers.h
```

# License

See the file [LICENSE.txt](LICENSE.txt) for license information.
