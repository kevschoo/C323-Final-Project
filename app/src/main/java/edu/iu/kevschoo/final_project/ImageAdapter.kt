import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.iu.kevschoo.final_project.databinding.ItemImageBinding

class ImageAdapter(private var images: List<String>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    /**
     * ViewHolder for image items, holding the view binding
     */
    class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Creates new ViewHolder instances for RecyclerView items
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds the View for each item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder
    {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemImageBinding.inflate(layoutInflater, parent, false)
        return ImageViewHolder(binding)
    }

    /**
     * Binds the data at the specified position into the ViewHolder
     * @param holder The ViewHolder that should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int)
    {
        val imageUrl = images[position]
        Glide.with(holder.binding.imageViewItem.context).load(imageUrl).into(holder.binding.imageViewItem)
    }

    /**
     * Returns the total number of items in the data set held by the adapter
     * @return The total number of items in this adapter
     */
    override fun getItemCount(): Int = images.size

    /**
     * Updates the list of image URLs and notifies the adapter to refresh the view
     * @param newImages The new list of image URLs to display
     */
    fun updateImages(newImages: List<String>)
    {
        images = newImages
        Log.d("ImageAdapter", "Number of images loaded: ${images.size}")
        notifyDataSetChanged()
    }
}