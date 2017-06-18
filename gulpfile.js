const gulp = require('gulp');
const babel = require('gulp-babel');

// default task
gulp.task('default', ['babel', 'watch'])

// compile es6->es5 using babel
gulp.task('babel', () =>
    gulp.src('es6scripts/**/*.js')
        .pipe(babel({
            presets: ['env']
        }))
        .pipe(gulp.dest('target/web/public/main/es6scripts'))
);

// watch for file saves
gulp.task('watch', () =>
    gulp.watch('es6scripts/**/*.js', ['babel'])
);