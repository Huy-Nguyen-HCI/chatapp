const gulp = require('gulp')
const babel = require('gulp-babel')

gulp.task('default', () =>
    gulp.src('es6scripts/**/*.js')
        .pipe(babel({
            presets: ['env']
        }))
        .pipe(gulp.dest('target/web/public/main/es6scripts'))
);