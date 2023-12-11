import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.iu.kevschoo.final_project.databinding.ItemImageBinding

class ImageAdapter(private var images: List<String>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemImageBinding.inflate(layoutInflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int)
    {
        val imageUrl = images[position]
        Glide.with(holder.binding.imageViewItem.context).load(imageUrl).into(holder.binding.imageViewItem)
    }

    override fun getItemCount(): Int = images.size

    fun updateImages(newImages: List<String>)
    {
        images = newImages
        Log.d("ImageAdapter", "Number of images loaded: ${images.size}")
        notifyDataSetChanged()
    }
}