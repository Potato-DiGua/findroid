package dev.jdtech.jellyfin.ui.activities.player

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.jdtech.jellyfin.databinding.ActivityIjkPlayerBinding
import dev.jdtech.jellyfin.models.PlayerItem
import dev.jdtech.jellyfin.ui.fragments.mediainfo.MediaInfoViewModel


@AndroidEntryPoint
class IjkPlayerActivity : AppCompatActivity() {
    companion object {
        const val INTENT_KEY_PLAYER_ITEM = "PLAYER_ITEM"
    }

    lateinit var binding: ActivityIjkPlayerBinding
    private lateinit var playerItem: List<PlayerItem>
    private val viewModel: IjkPlayerActivityViewModel by viewModels()
    private val mediaInfoViewModel: MediaInfoViewModel by viewModels()

    private val args: IjkPlayerActivityArgs by navArgs()

    private var isPlay = false
    private var isPause = false

    private var orientationUtils: OrientationUtils? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIjkPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topSpace.layoutParams = binding.topSpace.layoutParams.apply {
            height = BarUtils.getStatusBarHeight()
        }
        binding.videoPlayer.apply {
            layoutParams.height = (ScreenUtils.getAppScreenWidth() * 9 / 16.0f).toInt()
        }

        initVideoPlayer()
        init()


    }

    private fun init() {
//        val extra = intent.getParcelableArrayExtra(
//            INTENT_KEY_PLAYER_ITEM
//        )
//        if (extra.isNullOrEmpty()) {
//            finish()
//            return
//        }
//        playerItem = extra.map { it as PlayerItem }.toList()
        viewModel.mediaItems.observe(this) {
            binding.videoPlayer.setUp(
                it[0].playbackProperties?.uri.toString(),
                true,
                mediaInfoViewModel.playerItems[0].name ?: ""
            )
        }
        mediaInfoViewModel.navigateToPlayer.observe(this) {
            if (!flag && !it.isNullOrEmpty()) {
                viewModel.initializePlayer(it.toList())
                flag = true
            }
        }
        mediaInfoViewModel.item.observe(this) {
            mediaInfoViewModel.preparePlayerItems()
        }
        mediaInfoViewModel.loadData(args.itemId, args.itemType)

    }

    private var flag = false

    private fun initVideoPlayer() {
        val detailPlayer = binding.videoPlayer

        //外部辅助的旋转，帮助全屏
        //orientationUtils = new OrientationUtils(this, detailPlayer);
        //初始化不打开外部的旋转
        // orientationUtils.setEnable(false);


        //外部辅助的旋转，帮助全屏
        orientationUtils = OrientationUtils(this, detailPlayer);
        //初始化不打开外部的旋转
        // orientationUtils.setEnable(false);
        GSYVideoOptionBuilder()
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(false)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setCacheWithPlay(false)
//            .setNeedOrientationUtils(false)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onPrepared(url: String, vararg objects: Any) {
                    super.onPrepared(url, *objects)
                    //开始播放了才能旋转和全屏
                    //orientationUtils.setEnable(detailPlayer.isRotateWithSystem());
                    isPlay = true
                }

                override fun onQuitFullscreen(url: String, vararg objects: Any) {
                    super.onQuitFullscreen(url, *objects)
                    // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                    // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                    orientationUtils?.backToProtVideo();
                }
            }).setLockClickListener { view, lock ->
                //配合下方的onConfigurationChanged
                orientationUtils?.isEnable = !lock;
            }.build(detailPlayer)

        detailPlayer.fullscreenButton.setOnClickListener {
            //直接横屏
            // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
            // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
            orientationUtils?.resolveByClick();
            //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
            detailPlayer.startWindowFullscreen(this, true, true)
        }
        detailPlayer.backButton.setOnClickListener {
            finish()
        }
    }


    override fun onBackPressed() {
        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        orientationUtils?.backToProtVideo();
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }


    override fun onPause() {
        binding.videoPlayer.currentPlayer.onVideoPause()
        super.onPause()
        isPause = true
    }

    override fun onResume() {
        binding.videoPlayer.currentPlayer.onVideoResume(false)
        super.onResume()
        isPause = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlay) {
            binding.videoPlayer.currentPlayer.release()
        }
//        if (orientationUtils != null)
//            orientationUtils.releaseListener();
    }


    /**
     * orientationUtils 和  detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            binding.videoPlayer.onConfigurationChanged(
                this,
                newConfig,
                orientationUtils,
                true,
                true
            );
        }
    }
}