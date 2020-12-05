package cmsc436.semesterproject.localapparel

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder

// CITATION: Done with the help of https://medium.com/@mxcsyounes/the-easiest-way-to-build-intro-sliders-in-android-in-3-steps-3d6c952153e8
// CITATION: Component from https://github.com/AppIntro/AppIntro
class IntroActivity: AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        showIntroSlides()
    }

    private fun showIntroSlides() {
        val intro1 = SliderPagerBuilder()
            .title(getString(R.string.intro_slide1_title))
            .description(getString(R.string.intro_slide1_desc))
            .imageDrawable(R.drawable.clothes)
            .bgColor(getColor(R.color.introSlide1))
            .build()

        val intro2 = SliderPagerBuilder()
            .title(getString(R.string.intro_slide2_title))
            .description(getString(R.string.intro_slide2_desc))
            .imageDrawable(R.drawable.location)
            .bgColor(getColor(R.color.introSlide2))
            .build()

        val intro3 = SliderPagerBuilder()
            .title(getString(R.string.intro_slide3_title))
            .description(getString(R.string.intro_slide3_desc))
            .imageDrawable(R.drawable.intro_add)
            .bgColor(getColor(R.color.introSlide3))
            .build()

        val intro4 = SliderPagerBuilder()
            .title(getString(R.string.intro_slide4_title))
            .description(getString(R.string.intro_slide4_desc))
            .imageDrawable(R.drawable.logo)
            .bgColor(getColor(R.color.introSlide4))
            .build()

        addSlide(AppIntro2Fragment.newInstance(intro1))
        addSlide(AppIntro2Fragment.newInstance(intro2))
        addSlide(AppIntro2Fragment.newInstance(intro3))
        addSlide(AppIntro2Fragment.newInstance(intro4))

        showStatusBar(false)

        setFadeAnimation()

    }
    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        goToMain()
    }
}